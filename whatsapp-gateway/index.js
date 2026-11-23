/*
 * Gateway de WhatsApp do Easy Eats (Baileys).
 *
 * Conecta UM número comum de WhatsApp pelo QR Code (como no WhatsApp Web) e expõe uma API HTTP mínima,
 * usada só pela API do Easy Eats (módulo integracao.whatsapp):
 *   GET  /health              sem autenticação, para o Railway
 *   GET  /status              { status, numero, qr }   (qr = texto do QR Code enquanto não conectado)
 *   POST /send                { number, text }          envia uma mensagem de texto
 *   POST /logout              desconecta o número e gera um QR Code novo
 * Todas as rotas, menos /health, exigem o header "x-api-key" igual à variável API_KEY.
 *
 * A sessão fica em AUTH_DIR (no Railway, um volume em /data), então o número continua conectado após um deploy.
 */
const http = require('node:http');
const fs = require('node:fs');
const crypto = require('node:crypto');
const pino = require('pino');
const {
  default: makeWASocket,
  useMultiFileAuthState,
  fetchLatestBaileysVersion,
  DisconnectReason,
  Browsers,
} = require('@whiskeysockets/baileys');

const PORT = Number(process.env.PORT || 3000);
const API_KEY = process.env.API_KEY || '';
const AUTH_DIR = process.env.AUTH_DIR || './auth';
const log = pino({ level: process.env.LOG_LEVEL || 'info' });

if (API_KEY.length < 16) {
  log.fatal('Defina a variável API_KEY (mínimo de 16 caracteres)');
  process.exit(1);
}

const estado = { status: 'iniciando', qr: null, numero: null, erro: null };
let sock = null;
let reconectando = null;

async function conectar() {
  fs.mkdirSync(AUTH_DIR, { recursive: true });
  const { state, saveCreds } = await useMultiFileAuthState(AUTH_DIR);
  let version;
  try {
    ({ version } = await fetchLatestBaileysVersion());
  } catch {
    version = undefined; // usa a versão embutida na biblioteca
  }

  estado.status = 'conectando';
  sock = makeWASocket({
    version,
    auth: state,
    logger: log.child({ modulo: 'baileys' }, { level: 'warn' }),
    browser: Browsers.ubuntu('Easy Eats'),
    printQRInTerminal: false,
    markOnlineOnConnect: false,
    syncFullHistory: false,
  });

  sock.ev.on('creds.update', saveCreds);
  sock.ev.on('connection.update', (update) => {
    const { connection, lastDisconnect, qr } = update;
    if (qr) {
      estado.status = 'aguardando_qr';
      estado.qr = qr;
      log.info('QR Code novo gerado: escaneie pela tela de integração do Easy Eats');
    }
    if (connection === 'open') {
      estado.status = 'conectado';
      estado.qr = null;
      estado.erro = null;
      estado.numero = (sock.user?.id || '').split(':')[0].split('@')[0] || null;
      log.info({ numero: mascarar(estado.numero) }, 'WhatsApp conectado');
    }
    if (connection === 'close') {
      const codigo = lastDisconnect?.error?.output?.statusCode;
      estado.numero = null;
      if (codigo === DisconnectReason.loggedOut) {
        log.warn('Sessão encerrada pelo celular: apagando a sessão e gerando um QR Code novo');
        limparSessao();
      }
      estado.status = 'desconectado';
      estado.erro = lastDisconnect?.error?.message || null;
      agendarReconexao(codigo === DisconnectReason.restartRequired ? 0 : 3000);
    }
  });
}

function agendarReconexao(ms) {
  if (reconectando) return;
  reconectando = setTimeout(() => {
    reconectando = null;
    conectar().catch((e) => {
      log.error({ err: e }, 'Falha ao conectar');
      agendarReconexao(10000);
    });
  }, ms);
}

function limparSessao() {
  fs.rmSync(AUTH_DIR, { recursive: true, force: true });
}

function mascarar(numero) {
  return numero ? '****' + String(numero).slice(-4) : null;
}

// ------------------------------------------------------------------ HTTP

function autorizado(req) {
  const recebida = Buffer.from(String(req.headers['x-api-key'] || ''));
  const esperada = Buffer.from(API_KEY);
  return recebida.length === esperada.length && crypto.timingSafeEqual(recebida, esperada);
}

function responder(res, status, corpo) {
  res.writeHead(status, { 'Content-Type': 'application/json; charset=utf-8' });
  res.end(JSON.stringify(corpo));
}

function lerJson(req) {
  return new Promise((resolve, reject) => {
    let dados = '';
    req.on('data', (parte) => {
      dados += parte;
      if (dados.length > 64 * 1024) {
        reject(new Error('Corpo muito grande'));
        req.destroy();
      }
    });
    req.on('end', () => {
      try {
        resolve(dados ? JSON.parse(dados) : {});
      } catch {
        reject(new Error('JSON inválido'));
      }
    });
    req.on('error', reject);
  });
}

async function enviar(numero, texto) {
  if (estado.status !== 'conectado' || !sock) {
    const erro = new Error('WhatsApp não está conectado. Escaneie o QR Code na tela de integração.');
    erro.status = 503;
    throw erro;
  }
  const digitos = String(numero || '').replace(/\D/g, '');
  if (digitos.length < 12 || digitos.length > 15 || !texto) {
    const erro = new Error('Informe number (com DDI) e text');
    erro.status = 400;
    throw erro;
  }
  // onWhatsApp resolve o JID certo (inclusive o 9º dígito dos celulares do Brasil)
  const [contato] = await sock.onWhatsApp(digitos);
  if (!contato?.exists) {
    const erro = new Error('Este número não tem WhatsApp');
    erro.status = 404;
    throw erro;
  }
  const enviado = await sock.sendMessage(contato.jid, { text: String(texto).slice(0, 4000) });
  return enviado?.key?.id || null;
}

const servidor = http.createServer(async (req, res) => {
  const url = new URL(req.url, 'http://localhost');
  try {
    if (req.method === 'GET' && url.pathname === '/health') {
      return responder(res, 200, { ok: true });
    }
    if (!autorizado(req)) {
      return responder(res, 401, { erro: 'Não autorizado' });
    }
    if (req.method === 'GET' && url.pathname === '/status') {
      return responder(res, 200, {
        status: estado.status,
        numero: estado.numero,
        qr: estado.qr,
        erro: estado.status === 'conectado' ? null : estado.erro,
      });
    }
    if (req.method === 'POST' && url.pathname === '/send') {
      const corpo = await lerJson(req);
      const id = await enviar(corpo.number, corpo.text);
      log.info({ para: mascarar(corpo.number) }, 'Mensagem enviada');
      return responder(res, 200, { enviado: true, id });
    }
    if (req.method === 'POST' && url.pathname === '/logout') {
      try {
        await sock?.logout();
      } catch {
        /* já desconectado */
      }
      limparSessao();
      estado.status = 'desconectado';
      estado.numero = null;
      estado.qr = null;
      agendarReconexao(500);
      return responder(res, 200, { ok: true });
    }
    return responder(res, 404, { erro: 'Rota não encontrada' });
  } catch (e) {
    log.warn({ err: e.message }, 'Erro na requisição');
    return responder(res, e.status || 500, { erro: e.message || 'Erro interno' });
  }
});

servidor.listen(PORT, () => log.info(`Gateway de WhatsApp ouvindo na porta ${PORT}`));
conectar().catch((e) => {
  log.error({ err: e }, 'Falha ao conectar');
  agendarReconexao(10000);
});

process.on('SIGTERM', () => {
  servidor.close();
  process.exit(0);
});
