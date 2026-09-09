package com.easy.eats.empresa.service;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.easy.eats.empresa.model.model.Empresa;
import com.easy.eats.empresa.repository.EmpresaRepository;
import com.easy.eats.notificacao.service.NotificacaoService;
import com.easy.eats.security.SecurityUtils;
import com.easy.eats.segmento.model.Funcionalidade;
import com.easy.eats.segmento.model.Segmento;
import com.easy.eats.segmento.repository.SegmentoRepository;

@Service
public class EmpresaService {
    private final EmpresaRepository repository;
    private final SegmentoRepository segmentoRepository;
    private final NotificacaoService notificacaoService;

    public EmpresaService(EmpresaRepository repository, SegmentoRepository segmentoRepository,
            NotificacaoService notificacaoService) {
        this.repository = repository;
        this.segmentoRepository = segmentoRepository;
        this.notificacaoService = notificacaoService;
    }

    public List<Empresa> listarTodos() {
        return repository.findAll();
    }

    public Empresa buscarPorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa não encontrada"));
    }

    public Empresa buscarPorSlug(String slug) {
        return repository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Empresa não encontrada"));
    }

    public Empresa salvar(Empresa empresa) {
        empresa.setId(null);
        empresa.setSegmento(segmentoDoIdRecebido(empresa.getSegmento()));
        empresa.setSlug(gerarSlugUnico(empresa.getNome(), null));
        // Datas são responsabilidade do servidor: o cadastro pelo frontend não
        // envia (nem deveria enviar) dtCriacao/dtAlteracao.
        empresa.setDtCriacao(LocalDateTime.now());
        empresa.setDtAlteracao(LocalDateTime.now());
        Empresa salva = repository.save(empresa);

        notificacaoService.notificarPlataforma("bi-building-add", "azul", "Nova empresa cadastrada",
                "\"" + salva.getNome() + "\" se cadastrou na plataforma.");

        return salva;
    }

    /**
     * Permite ao ADMINISTRADOR da própria empresa (ou ao SUPERADMIN, para
     * qualquer empresa) trocar o link público do cardápio. A autorização de
     * "só a própria empresa" já é reforçada no SecurityConfig via o matcher
     * de PUT /empresa/{id}/slug, mas o service reforça de novo aqui porque um
     * ADMINISTRADOR de uma empresa não deve conseguir alterar o slug de outra
     * só por adivinhar o id na URL.
     */
    public Empresa atualizarSlug(Integer id, String novoSlug) {
        Empresa empresa = buscarPorId(id);

        if (!SecurityUtils.isSuperadmin() && !Objects.equals(id, SecurityUtils.getEmpresaId())) {
            throw new IllegalArgumentException("Você não tem permissão para alterar esta empresa");
        }

        if (novoSlug == null || novoSlug.isBlank()) {
            throw new IllegalArgumentException("O link público não pode ficar vazio");
        }

        String slugNormalizado = normalizarSlug(novoSlug);
        if (repository.findBySlug(slugNormalizado).filter(e -> !e.getId().equals(id)).isPresent()) {
            throw new IllegalArgumentException("Esse link já está em uso por outra empresa");
        }

        empresa.setSlug(slugNormalizado);
        return repository.save(empresa);
    }

    private String gerarSlugUnico(String nome, Integer idAtual) {
        String base = normalizarSlug(nome);
        String candidato = base;
        int sufixo = 2;
        while (repository.findBySlug(candidato).filter(e -> !e.getId().equals(idAtual)).isPresent()) {
            candidato = base + "-" + sufixo;
            sufixo++;
        }
        return candidato;
    }

    private String normalizarSlug(String texto) {
        String semAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcentos.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("[\\s-]+", "-")
                .replaceAll("^-|-$", "");
    }

    public Empresa atualizar(Integer id, Empresa empresa) {

        Empresa empresaExistente = buscarPorId(id);

        empresaExistente.setNome(empresa.getNome());
        empresaExistente.setCnpj(empresa.getCnpj());
        empresaExistente.setEmail(empresa.getEmail());
        empresaExistente.setTelefone(empresa.getTelefone());
        empresaExistente.setFlAtivo(empresa.getFlAtivo());
        // dtCriacao é imutável: copiá-la do corpo apagava a data original toda
        // vez que a tela de empresas salvava (o payload não inclui esse campo).
        empresaExistente.setDtAlteracao(LocalDateTime.now());
        empresaExistente.setSegmento(segmentoDoIdRecebido(empresa.getSegmento()));

        return repository.save(empresaExistente);
    }

    public void deletar(Integer id) {
        repository.deleteById(id);
    }

    /**
     * Autoatendimento do ADMINISTRADOR sobre a própria empresa — o resto de
     * /empresa/** é SUPERADMIN-only, então sem isso a empresa cliente não
     * tinha como ver ou editar nem os próprios dados de estabelecimento
     * (nome, endereço, horário de funcionamento).
     */
    public Empresa buscarMinhaEmpresa() {
        return buscarPorId(SecurityUtils.getEmpresaId());
    }

    public Empresa atualizarMinhaEmpresa(Empresa dados) {
        Empresa existente = buscarMinhaEmpresa();

        existente.setNome(dados.getNome());
        existente.setTelefone(dados.getTelefone());
        existente.setEndereco(dados.getEndereco());
        existente.setHorarioFuncionamento(dados.getHorarioFuncionamento());
        existente.setDtAlteracao(LocalDateTime.now());

        return repository.save(existente);
    }

    /**
     * Funcionalidades liberadas para a empresa, de acordo com o segmento de
     * negócio vinculado a ela. Sem segmento definido, libera tudo (evita
     * quebrar empresas cadastradas antes dessa configuração existir).
     */
    public List<Funcionalidade> funcionalidadesHabilitadas(Empresa empresa) {
        if (empresa == null || empresa.getSegmento() == null || empresa.getSegmento().getFuncionalidades() == null
                || empresa.getSegmento().getFuncionalidades().isEmpty()) {
            return Arrays.asList(Funcionalidade.values());
        }
        return empresa.getSegmento().getFuncionalidades().stream().toList();
    }

    private Segmento segmentoDoIdRecebido(Segmento segmentoRecebido) {
        if (segmentoRecebido == null || segmentoRecebido.getId() == null) {
            return null;
        }
        return segmentoRepository.findById(segmentoRecebido.getId())
                .orElseThrow(() -> new IllegalArgumentException("Segmento informado não existe"));
    }
}
