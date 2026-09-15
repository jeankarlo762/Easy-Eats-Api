package com.easy.eats.security;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.easy.eats.caixa.enums.StatusCaixa;
import com.easy.eats.caixa.model.Caixa;
import com.easy.eats.caixa.repository.CaixaRepository;
import com.easy.eats.cashback.model.CashbackConfig;
import com.easy.eats.cashback.repository.CashbackConfigRepository;
import com.easy.eats.categoria.model.Categoria;
import com.easy.eats.categoria.repository.CategoriaRepository;
import com.easy.eats.cliente.model.Cliente;
import com.easy.eats.cliente.repository.ClienteRepository;
import com.easy.eats.comanda.enums.StatusComanda;
import com.easy.eats.comanda.model.Comanda;
import com.easy.eats.comanda.repository.ComandaRepository;
import com.easy.eats.cupom.enums.TipoDesconto;
import com.easy.eats.cupom.model.Cupom;
import com.easy.eats.cupom.repository.CupomRepository;
import com.easy.eats.despesa.model.Despesa;
import com.easy.eats.despesa.repository.DespesaRepository;
import com.easy.eats.empresa.model.model.Empresa;
import com.easy.eats.empresa.repository.EmpresaRepository;
import com.easy.eats.endereco.model.Endereco;
import com.easy.eats.entrega.enums.StatusEntrega;
import com.easy.eats.entrega.model.Entrega;
import com.easy.eats.entrega.repository.EntregaRepository;
import com.easy.eats.entregador.model.Entregador;
import com.easy.eats.entregador.repository.EntregadorRepository;
import com.easy.eats.estoque.enums.TipoMovimentacaoEstoque;
import com.easy.eats.estoque.model.Estoque;
import com.easy.eats.estoque.model.MovimentacaoEstoque;
import com.easy.eats.estoque.repository.EstoqueRepository;
import com.easy.eats.estoque.repository.MovimentacaoEstoqueRepository;
import com.easy.eats.fichatecnica.model.FichaTecnicaItem;
import com.easy.eats.fichatecnica.repository.FichaTecnicaItemRepository;
import com.easy.eats.fornecedor.model.Fornecedor;
import com.easy.eats.fornecedor.repository.FornecedorRepository;
import com.easy.eats.itemVenda.model.ItemVenda;
import com.easy.eats.itemVenda.repository.ItemVendaRepository;
import com.easy.eats.mesa.model.Mesa;
import com.easy.eats.mesa.repository.MesaRepository;
import com.easy.eats.movimentacaoFinanceira.enums.TipoMovimentacao;
import com.easy.eats.movimentacaoFinanceira.model.MovimentacaoFinanceira;
import com.easy.eats.movimentacaoFinanceira.repository.MovimentacaoFinanceiraRepository;
import com.easy.eats.notificacao.model.Notificacao;
import com.easy.eats.notificacao.repository.NotificacaoRepository;
import com.easy.eats.pedidocompra.enums.StatusPedidoCompra;
import com.easy.eats.pedidocompra.model.PedidoCompra;
import com.easy.eats.pedidocompra.repository.PedidoCompraRepository;
import com.easy.eats.produto.enums.NaturezaProduto;
import com.easy.eats.produto.model.Produto;
import com.easy.eats.produto.repository.ProdutoRepository;
import com.easy.eats.usuario.model.Usuario;
import com.easy.eats.usuario.repository.UsuarioRepository;
import com.easy.eats.venda.repository.VendaRepository;

/**
 * Amplia os dados de demonstração além do que o DataSeeder cria (que só
 * cobre empresa/mesas/catálogo básico e os 5 usuários de acesso). Preenche
 * clientes, fornecedores, estoque/insumos, ficha técnica, comandas e vendas
 * em vários status, cupons, cashback, delivery, despesas, pedidos de compra,
 * notificações e uma sessão de caixa — para dar visibilidade real de cada
 * tela do sistema.
 *
 * Cada bloco tem sua própria checagem "já existe?" (em vez de um único
 * corte no topo) porque uma falha no meio do seeder — por exemplo, uma
 * exceção não tratada, que aqui derruba a aplicação e faz a Railway
 * reiniciar o container — não pode deixar metade dos blocos vazios para
 * sempre: sem essas checagens por bloco, um único corte global no início
 * (baseado só na existência de clientes, por exemplo) faria os blocos
 * seguintes serem pulados silenciosamente em toda tentativa futura assim
 * que o primeiro bloco terminasse, mesmo que os últimos nunca tivessem
 * rodado.
 */
@Component
@Order(2)
public class DemoDataExpandidoSeeder implements CommandLineRunner {

    private final EmpresaRepository empresaRepository;
    private final MesaRepository mesaRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final FornecedorRepository fornecedorRepository;
    private final EstoqueRepository estoqueRepository;
    private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;
    private final FichaTecnicaItemRepository fichaTecnicaItemRepository;
    private final ComandaRepository comandaRepository;
    private final VendaRepository vendaRepository;
    private final ItemVendaRepository itemVendaRepository;
    private final CupomRepository cupomRepository;
    private final CashbackConfigRepository cashbackConfigRepository;
    private final EntregadorRepository entregadorRepository;
    private final EntregaRepository entregaRepository;
    private final DespesaRepository despesaRepository;
    private final PedidoCompraRepository pedidoCompraRepository;
    private final NotificacaoRepository notificacaoRepository;
    private final CaixaRepository caixaRepository;
    private final MovimentacaoFinanceiraRepository movimentacaoFinanceiraRepository;

    public DemoDataExpandidoSeeder(EmpresaRepository empresaRepository, MesaRepository mesaRepository,
            CategoriaRepository categoriaRepository, ProdutoRepository produtoRepository,
            UsuarioRepository usuarioRepository, ClienteRepository clienteRepository,
            FornecedorRepository fornecedorRepository, EstoqueRepository estoqueRepository,
            MovimentacaoEstoqueRepository movimentacaoEstoqueRepository,
            FichaTecnicaItemRepository fichaTecnicaItemRepository, ComandaRepository comandaRepository,
            VendaRepository vendaRepository, ItemVendaRepository itemVendaRepository,
            CupomRepository cupomRepository, CashbackConfigRepository cashbackConfigRepository,
            EntregadorRepository entregadorRepository, EntregaRepository entregaRepository,
            DespesaRepository despesaRepository, PedidoCompraRepository pedidoCompraRepository,
            NotificacaoRepository notificacaoRepository, CaixaRepository caixaRepository,
            MovimentacaoFinanceiraRepository movimentacaoFinanceiraRepository) {
        this.empresaRepository = empresaRepository;
        this.mesaRepository = mesaRepository;
        this.categoriaRepository = categoriaRepository;
        this.produtoRepository = produtoRepository;
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.fornecedorRepository = fornecedorRepository;
        this.estoqueRepository = estoqueRepository;
        this.movimentacaoEstoqueRepository = movimentacaoEstoqueRepository;
        this.fichaTecnicaItemRepository = fichaTecnicaItemRepository;
        this.comandaRepository = comandaRepository;
        this.vendaRepository = vendaRepository;
        this.itemVendaRepository = itemVendaRepository;
        this.cupomRepository = cupomRepository;
        this.cashbackConfigRepository = cashbackConfigRepository;
        this.entregadorRepository = entregadorRepository;
        this.entregaRepository = entregaRepository;
        this.despesaRepository = despesaRepository;
        this.pedidoCompraRepository = pedidoCompraRepository;
        this.notificacaoRepository = notificacaoRepository;
        this.caixaRepository = caixaRepository;
        this.movimentacaoFinanceiraRepository = movimentacaoFinanceiraRepository;
    }

    @Override
    public void run(String... args) {
        Empresa empresa = empresaRepository.findAll().stream()
                .filter(e -> "Empresa Demo".equals(e.getNome()))
                .findFirst()
                .orElse(null);
        if (empresa == null) {
            return;
        }

        Usuario admin = usuarioRepository.findByEmail("admin@easyeats.com").orElseThrow();
        Usuario operador = usuarioRepository.findByEmail("operador@easyeats.com").orElseThrow();
        List<Mesa> mesas = mesaRepository.findAllByEmpresaId(empresa.getId());

        // Catálogo extra (insumos + produtos adicionais) e ficha técnica: cada
        // produto/categoria é find-or-create por nome, então é seguro rodar de
        // novo mesmo que um bloco mais à frente tenha falhado antes.
        Categoria insumos = categoriaOuCriar("Insumos", empresa);
        Produto pao = produtoOuCriar("Pão de Hambúrguer", null, new BigDecimal("0.90"), NaturezaProduto.INSUMO, insumos, empresa);
        Produto carne = produtoOuCriar("Carne de Hambúrguer 180g", null, new BigDecimal("6.50"), NaturezaProduto.INSUMO, insumos, empresa);
        Produto queijo = produtoOuCriar("Queijo Cheddar Fatia", null, new BigDecimal("0.80"), NaturezaProduto.INSUMO, insumos, empresa);

        Categoria lanches = categoriaOuCriar("Lanches", empresa);
        Categoria bebidas = categoriaOuCriar("Bebidas", empresa);
        Produto batataFrita = produtoOuCriar("Batata Frita", "Porção individual crocante", new BigDecimal("14.00"),
                NaturezaProduto.PREPARADO, lanches, empresa);
        Produto sucoLaranja = produtoOuCriar("Suco de Laranja", "300ml natural", new BigDecimal("9.00"),
                NaturezaProduto.REVENDA, bebidas, empresa);

        Produto hamburguerClassico = produtoPorNome(empresa, "Hambúrguer Clássico");
        Produto cocaCola = produtoPorNome(empresa, "Coca-Cola");
        Produto aguaMineral = produtoPorNome(empresa, "Água Mineral");

        fichaTecnicaItemSeNaoExistir(hamburguerClassico, pao, new BigDecimal("1"));
        fichaTecnicaItemSeNaoExistir(hamburguerClassico, carne, new BigDecimal("1"));
        fichaTecnicaItemSeNaoExistir(hamburguerClassico, queijo, new BigDecimal("2"));

        estoqueSeNaoExistir(pao, 80, 20);
        estoqueSeNaoExistir(carne, 60, 15);
        estoqueSeNaoExistir(queijo, 120, 30);
        estoqueSeNaoExistir(cocaCola, 48, 12);
        estoqueSeNaoExistir(aguaMineral, 36, 10);
        estoqueSeNaoExistir(batataFrita, 25, 10);

        Fornecedor fornecedorCarnes = fornecedorOuCriar("Frigorífico Bom Corte", "12345678000190", "(11) 4002-8922", empresa);
        Fornecedor fornecedorBebidas = fornecedorOuCriar("Distribuidora Refresco Total", "23456789000181", "(11) 4003-1234", empresa);
        Fornecedor fornecedorHortifruti = fornecedorOuCriar("Hortifruti Sabor da Terra", "34567890000172", "(11) 4004-5566", empresa);

        // Histórico de movimentação e pedidos de compra não têm chave natural
        // para dedupe — roda uma única vez, guardado pela presença de pedidos
        // de compra da empresa.
        if (pedidoCompraRepository.findAllByEmpresaIdOrderByDtCriacaoDesc(empresa.getId()).isEmpty()) {
            movimentacaoEstoque(carne, TipoMovimentacaoEstoque.ENTRADA, 60, "Compra inicial - " + fornecedorCarnes.getNome(), empresa);
            movimentacaoEstoque(pao, TipoMovimentacaoEstoque.ENTRADA, 80, "Compra inicial - padaria parceira", empresa);
            movimentacaoEstoque(cocaCola, TipoMovimentacaoEstoque.ENTRADA, 48, "Compra inicial - " + fornecedorBebidas.getNome(), empresa);
            movimentacaoEstoque(batataFrita, TipoMovimentacaoEstoque.SAIDA, 5, "Perda por validade", empresa);

            pedidoCompra(fornecedorCarnes, "60kg de carne de hambúrguer 180g", new BigDecimal("390.00"), StatusPedidoCompra.RECEBIDO, empresa);
            pedidoCompra(fornecedorBebidas, "10 fardos de Coca-Cola 350ml", new BigDecimal("280.00"), StatusPedidoCompra.ENVIADO, empresa);
            pedidoCompra(fornecedorHortifruti, "Hortifruti da semana (alface, tomate, cebola)", new BigDecimal("145.00"), StatusPedidoCompra.AGUARDANDO, empresa);
        }

        Cliente joao = clienteOuCriar("João Pereira", "joao.pereira@example.com", "(11) 98888-1111", "11122233344", empresa);
        enderecoSeNaoExistir(joao, "Rua das Flores", 120, "Centro", "São Paulo", "01001000");
        Cliente maria = clienteOuCriar("Maria Souza", "maria.souza@example.com", "(11) 98888-2222", "22233344455", empresa);
        enderecoSeNaoExistir(maria, "Av. Paulista", 900, "Bela Vista", "São Paulo", "01310000");
        clienteOuCriar("Carlos Lima", "carlos.lima@example.com", "(11) 98888-3333", "33344455566", empresa);
        Cliente ana = clienteOuCriar("Ana Ribeiro", "ana.ribeiro@example.com", "(11) 98888-4444", "44455566677", empresa);
        enderecoSeNaoExistir(ana, "Rua Augusta", 500, "Consolação", "São Paulo", "01305000");

        cupomOuCriar("BEMVINDO10", TipoDesconto.PERCENTUAL, new BigDecimal("10"), new BigDecimal("30.00"), empresa);
        cupomOuCriar("FRETE5", TipoDesconto.VALOR_FIXO, new BigDecimal("5.00"), null, empresa);

        if (cashbackConfigRepository.findByEmpresaId(empresa.getId()) == null) {
            CashbackConfig cashbackConfig = new CashbackConfig();
            cashbackConfig.setPercentualAcumulo(new BigDecimal("5"));
            cashbackConfig.setValorMinimoParaAcumular(new BigDecimal("20.00"));
            cashbackConfig.setFlAtivo(true);
            cashbackConfig.setEmpresa(empresa);
            cashbackConfigRepository.save(cashbackConfig);
        }

        Entregador pedro = entregadorOuCriar("Pedro Motoboy", "(11) 97777-1111", empresa);
        Entregador lucas = entregadorOuCriar("Lucas Motoboy", "(11) 97777-2222", empresa);

        if (entregaRepository.findAllByEmpresaIdOrderByDtCriacaoDesc(empresa.getId()).isEmpty()) {
            entrega("Maria Souza", "Av. Paulista, 900 - Bela Vista, São Paulo", pedro, StatusEntrega.A_CAMINHO, empresa);
            entrega("Carlos Lima", "Rua Vergueiro, 300 - Liberdade, São Paulo", lucas, StatusEntrega.AGUARDANDO_RETIRADA, empresa);
            entrega("Ana Ribeiro", "Rua Augusta, 500 - Consolação, São Paulo", null, StatusEntrega.ENTREGUE, empresa);
        }

        if (despesaRepository.findAllByEmpresaIdOrderByDtDespesaDesc(empresa.getId()).isEmpty()) {
            despesa("Aluguel do ponto", "Fixa", new BigDecimal("3500.00"), LocalDate.now().minusDays(10), empresa);
            despesa("Conta de energia", "Utilidades", new BigDecimal("620.00"), LocalDate.now().minusDays(7), empresa);
            despesa("Manutenção da fritadeira", "Manutenção", new BigDecimal("180.00"), LocalDate.now().minusDays(3), empresa);
            despesa("Compra de embalagens", "Insumos", new BigDecimal("240.00"), LocalDate.now().minusDays(1), empresa);
        }

        if (notificacaoRepository.findAllByEmpresaId(empresa.getId()).isEmpty()) {
            notificacao("bi-cart-check", "success", "Novo pedido recebido", "Pedido de Maria Souza aguardando preparo", empresa);
            notificacao("bi-box-seam", "warning", "Estoque baixo", "Queijo Cheddar Fatia está próximo do mínimo", empresa);
            notificacao("bi-truck", "info", "Entrega a caminho", "Pedido de Maria Souza saiu para entrega", empresa);
        }

        // Comanda aberta na mesa 1 + comanda fechada na mesa 2, cada uma com sua venda/itens.
        if (mesas.size() >= 2 && comandaRepository.findAllByEmpresaId(empresa.getId()).isEmpty()) {
            Mesa mesa1 = mesas.get(0);
            Comanda comandaAberta = comanda(1, mesa1, "Grupo Mesa 1", admin, empresa, StatusComanda.ABERTA);
            venda(comandaAberta, mesa1, "Preparando", operador, null, List.of(
                    itemPayload(hamburguerClassico, 2, hamburguerClassico.getPreco()),
                    itemPayload(cocaCola, 2, cocaCola.getPreco())));

            Mesa mesa2 = mesas.get(1);
            Comanda comandaFechada = comanda(2, mesa2, "Grupo Mesa 2", operador, empresa, StatusComanda.FECHADA);
            comandaFechada.setDtFechamento(LocalDateTime.now().minusHours(2));
            comandaFechada.setValorTotal(new BigDecimal("50.00"));
            comandaRepository.save(comandaFechada);
            venda(comandaFechada, mesa2, "Entregue", operador, null, List.of(
                    itemPayload(batataFrita, 1, batataFrita.getPreco()),
                    itemPayload(aguaMineral, 2, aguaMineral.getPreco()),
                    itemPayload(sucoLaranja, 1, sucoLaranja.getPreco())));
        }

        // Pedidos de balcão/delivery fora de comanda, em vários estágios do fluxo de cozinha.
        boolean jaTemVendaDeBalcao = vendaRepository.findAllByEmpresaId(empresa.getId()).stream()
                .anyMatch(v -> "Balcão".equals(v.getTipo()));
        if (!jaTemVendaDeBalcao) {
            venda(null, null, "Aguardando", operador, "Cliente Balcão", List.of(
                    itemPayload(hamburguerClassico, 1, hamburguerClassico.getPreco())));
            venda(null, null, "Pronto", operador, "Retirada Rápida", List.of(
                    itemPayload(batataFrita, 2, batataFrita.getPreco())));
            venda(null, null, "Entregue", admin, joao.getNome(), List.of(
                    itemPayload(hamburguerClassico, 1, hamburguerClassico.getPreco()),
                    itemPayload(cocaCola, 1, cocaCola.getPreco())));
        }

        // Sessão de caixa já fechada, com uma sangria registrada, para o histórico do módulo Caixa.
        if (caixaRepository.findAllByEmpresaId(empresa.getId()).isEmpty()) {
            Caixa caixa = new Caixa();
            caixa.setStatus(StatusCaixa.FECHADO);
            caixa.setValorInicial(new BigDecimal("200.00"));
            caixa.setValorApuradoInformado(new BigDecimal("612.00"));
            caixa.setValorApuradoSistema(new BigDecimal("612.00"));
            caixa.setDiferenca(BigDecimal.ZERO);
            caixa.setDtAbertura(LocalDateTime.now().minusHours(6));
            caixa.setDtFechamento(LocalDateTime.now().minusMinutes(30));
            caixa.setObservacoesAbertura("Abertura do turno da tarde");
            caixa.setObservacoesFechamento("Fechamento sem divergências");
            caixa.setUsuarioAbertura(operador);
            caixa.setUsuarioFechamento(operador);
            caixa.setEmpresa(empresa);
            caixa = caixaRepository.save(caixa);

            MovimentacaoFinanceira sangria = new MovimentacaoFinanceira();
            sangria.setTipo(TipoMovimentacao.SANGRIA);
            sangria.setCategoria("Retirada para troco");
            sangria.setValor(new BigDecimal("50.00"));
            sangria.setDescricao("Sangria para reforço de troco em outra frente de caixa");
            sangria.setCaixa(caixa);
            sangria.setEmpresa(empresa);
            movimentacaoFinanceiraRepository.save(sangria);
        }
    }

    private Categoria categoriaOuCriar(String nome, Empresa empresa) {
        return categoriaRepository.findAllByEmpresaId(empresa.getId()).stream()
                .filter(c -> nome.equals(c.getNome()))
                .findFirst()
                .orElseGet(() -> {
                    Categoria categoria = new Categoria();
                    categoria.setNome(nome);
                    categoria.setFlativo(true);
                    categoria.setEmpresa(empresa);
                    return categoriaRepository.save(categoria);
                });
    }

    private Produto produtoOuCriar(String nome, String descricao, BigDecimal preco, NaturezaProduto natureza,
            Categoria categoria, Empresa empresa) {
        return produtoRepository.findAllByEmpresaId(empresa.getId()).stream()
                .filter(p -> nome.equals(p.getNome()))
                .findFirst()
                .orElseGet(() -> {
                    Produto produto = new Produto();
                    produto.setNome(nome);
                    produto.setDescricao(descricao);
                    produto.setPreco(preco);
                    produto.setFlAtivo(true);
                    produto.setNatureza(natureza);
                    produto.setCategoria(categoria);
                    produto.setEmpresa(empresa);
                    return produtoRepository.save(produto);
                });
    }

    private Produto produtoPorNome(Empresa empresa, String nome) {
        return produtoRepository.findAllByEmpresaId(empresa.getId()).stream()
                .filter(p -> nome.equals(p.getNome()))
                .findFirst()
                .orElseThrow();
    }

    private void fichaTecnicaItemSeNaoExistir(Produto produtoFinal, Produto insumo, BigDecimal quantidade) {
        boolean jaExiste = fichaTecnicaItemRepository.findAllByProdutoId(produtoFinal.getId()).stream()
                .anyMatch(item -> item.getInsumo() != null && item.getInsumo().getId().equals(insumo.getId()));
        if (jaExiste) {
            return;
        }
        FichaTecnicaItem item = new FichaTecnicaItem();
        item.setProduto(produtoFinal);
        item.setInsumo(insumo);
        item.setQuantidade(quantidade);
        fichaTecnicaItemRepository.save(item);
    }

    private void estoqueSeNaoExistir(Produto produto, int quantidadeAtual, int estoqueMinimo) {
        if (estoqueRepository.findByProdutoId(produto.getId()).isPresent()) {
            return;
        }
        Estoque estoque = new Estoque();
        estoque.setProduto(produto);
        estoque.setQuantidadeAtual(quantidadeAtual);
        estoque.setEstoqueMinimo(estoqueMinimo);
        estoque.setDtUltimaMovimentacao(LocalDateTime.now());
        estoqueRepository.save(estoque);
    }

    private void movimentacaoEstoque(Produto produto, TipoMovimentacaoEstoque tipo, int quantidade, String observacao,
            Empresa empresa) {
        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque();
        movimentacao.setProduto(produto);
        movimentacao.setTipo(tipo);
        movimentacao.setQuantidade(quantidade);
        movimentacao.setObservacao(observacao);
        movimentacao.setDtMovimentacao(LocalDateTime.now());
        movimentacao.setEmpresa(empresa);
        movimentacaoEstoqueRepository.save(movimentacao);
    }

    private Fornecedor fornecedorOuCriar(String nome, String cnpj, String telefone, Empresa empresa) {
        return fornecedorRepository.findAllByEmpresaId(empresa.getId()).stream()
                .filter(f -> cnpj.equals(f.getCnpj()))
                .findFirst()
                .orElseGet(() -> {
                    Fornecedor fornecedor = new Fornecedor();
                    fornecedor.setNome(nome);
                    fornecedor.setCnpj(cnpj);
                    fornecedor.setTelefone(telefone);
                    fornecedor.setFlAtivo(true);
                    fornecedor.setDtCriacao(LocalDateTime.now());
                    fornecedor.setEmpresa(empresa);
                    return fornecedorRepository.save(fornecedor);
                });
    }

    private void pedidoCompra(Fornecedor fornecedor, String itens, BigDecimal valorTotal, StatusPedidoCompra status,
            Empresa empresa) {
        PedidoCompra pedido = new PedidoCompra();
        pedido.setFornecedor(fornecedor);
        pedido.setItens(itens);
        pedido.setValorTotal(valorTotal);
        pedido.setStatus(status);
        pedido.setDtCriacao(LocalDateTime.now());
        pedido.setEmpresa(empresa);
        pedidoCompraRepository.save(pedido);
    }

    private Cliente clienteOuCriar(String nome, String email, String telefone, String cpf, Empresa empresa) {
        return clienteRepository.findAllByEmpresaId(empresa.getId()).stream()
                .filter(c -> email.equals(c.getEmail()))
                .findFirst()
                .orElseGet(() -> {
                    Cliente cliente = new Cliente();
                    cliente.setNome(nome);
                    cliente.setEmail(email);
                    cliente.setTelefone(telefone);
                    cliente.setCpf(cpf);
                    cliente.setEmpresa(empresa);
                    return clienteRepository.save(cliente);
                });
    }

    private void enderecoSeNaoExistir(Cliente cliente, String rua, int numero, String bairro, String cidade, String cep) {
        if (cliente.getEnderecos() != null && !cliente.getEnderecos().isEmpty()) {
            return;
        }
        Endereco endereco = new Endereco();
        endereco.setRua(rua);
        endereco.setNumero(numero);
        endereco.setBairro(bairro);
        endereco.setCidade(cidade);
        endereco.setCep(cep);
        endereco.setCliente(cliente);
        cliente.getEnderecos().add(endereco);
        clienteRepository.save(cliente);
    }

    private void cupomOuCriar(String codigo, TipoDesconto tipo, BigDecimal valorDesconto, BigDecimal valorMinimoPedido,
            Empresa empresa) {
        boolean jaExiste = cupomRepository.findAllByEmpresaId(empresa.getId()).stream()
                .anyMatch(c -> codigo.equals(c.getCodigo()));
        if (jaExiste) {
            return;
        }
        Cupom cupom = new Cupom();
        cupom.setCodigo(codigo);
        cupom.setTipoDesconto(tipo);
        cupom.setValorDesconto(valorDesconto);
        cupom.setValorMinimoPedido(valorMinimoPedido);
        cupom.setDtValidadeInicio(LocalDateTime.now().minusDays(1));
        cupom.setDtValidadeFim(LocalDateTime.now().plusMonths(1));
        cupom.setFlAtivo(true);
        cupom.setEmpresa(empresa);
        cupomRepository.save(cupom);
    }

    private Entregador entregadorOuCriar(String nome, String telefone, Empresa empresa) {
        return entregadorRepository.findAllByEmpresaId(empresa.getId()).stream()
                .filter(e -> nome.equals(e.getNome()))
                .findFirst()
                .orElseGet(() -> {
                    Entregador entregador = new Entregador();
                    entregador.setNome(nome);
                    entregador.setTelefone(telefone);
                    entregador.setFlAtivo(true);
                    entregador.setEmpresa(empresa);
                    return entregadorRepository.save(entregador);
                });
    }

    private void entrega(String nomeCliente, String endereco, Entregador entregador, StatusEntrega status, Empresa empresa) {
        Entrega entrega = new Entrega();
        entrega.setNomeCliente(nomeCliente);
        entrega.setEndereco(endereco);
        entrega.setEntregador(entregador);
        entrega.setStatus(status);
        entrega.setDtCriacao(LocalDateTime.now());
        if (status == StatusEntrega.A_CAMINHO || status == StatusEntrega.ENTREGUE) {
            entrega.setDtSaida(LocalDateTime.now().minusMinutes(20));
        }
        if (status == StatusEntrega.ENTREGUE) {
            entrega.setDtEntrega(LocalDateTime.now().minusMinutes(5));
        }
        entrega.setEmpresa(empresa);
        entregaRepository.save(entrega);
    }

    private void despesa(String descricao, String categoria, BigDecimal valor, LocalDate dtDespesa, Empresa empresa) {
        Despesa despesa = new Despesa();
        despesa.setDescricao(descricao);
        despesa.setCategoria(categoria);
        despesa.setValor(valor);
        despesa.setDtDespesa(dtDespesa);
        despesa.setEmpresa(empresa);
        despesaRepository.save(despesa);
    }

    private void notificacao(String icone, String cor, String titulo, String descricao, Empresa empresa) {
        Notificacao notificacao = new Notificacao();
        notificacao.setIcone(icone);
        notificacao.setCor(cor);
        notificacao.setTitulo(titulo);
        notificacao.setDescricao(descricao);
        notificacao.setLida(false);
        notificacao.setDtCriacao(LocalDateTime.now());
        notificacao.setEmpresa(empresa);
        notificacaoRepository.save(notificacao);
    }

    private Comanda comanda(int numero, Mesa mesa, String nomeCliente, Usuario usuarioAbertura, Empresa empresa,
            StatusComanda status) {
        Comanda comanda = new Comanda();
        comanda.setNumero(numero);
        comanda.setStatus(status);
        comanda.setNomeCliente(nomeCliente);
        comanda.setValorTotal(BigDecimal.ZERO);
        comanda.setDtAbertura(LocalDateTime.now().minusHours(1));
        comanda.setMesa(mesa);
        comanda.setUsuarioAbertura(usuarioAbertura);
        comanda.setEmpresa(empresa);
        return comandaRepository.save(comanda);
    }

    private record ItemPayload(Produto produto, double quantidade, BigDecimal precoUnitario) {
    }

    private ItemPayload itemPayload(Produto produto, double quantidade, BigDecimal precoUnitario) {
        return new ItemPayload(produto, quantidade, precoUnitario);
    }

    private void venda(Comanda comanda, Mesa mesa, String status, Usuario usuario, String nomeCliente,
            List<ItemPayload> itens) {
        com.easy.eats.venda.model.Venda venda = new com.easy.eats.venda.model.Venda();
        venda.setStatus(status);
        venda.setTipo(mesa != null ? "Mesa" : "Balcão");
        venda.setMesa(mesa);
        venda.setComanda(comanda);
        venda.setNomeCliente(nomeCliente);
        venda.setUsuario(usuario);

        BigDecimal total = itens.stream()
                .map(item -> item.precoUnitario().multiply(BigDecimal.valueOf(item.quantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        venda.setValor_total(total);
        venda = vendaRepository.save(venda);

        for (ItemPayload item : itens) {
            ItemVenda itemVenda = new ItemVenda();
            itemVenda.setVenda(venda);
            itemVenda.setProduto(item.produto());
            itemVenda.setQuantidade(item.quantidade());
            itemVenda.setPreco_unitario(item.precoUnitario());
            itemVenda.setValor_total(item.precoUnitario().multiply(BigDecimal.valueOf(item.quantidade())));
            itemVendaRepository.save(itemVenda);
        }
    }
}
