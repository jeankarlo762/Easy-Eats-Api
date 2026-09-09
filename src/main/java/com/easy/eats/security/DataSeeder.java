package com.easy.eats.security;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.easy.eats.categoria.model.Categoria;
import com.easy.eats.categoria.repository.CategoriaRepository;
import com.easy.eats.empresa.model.model.Empresa;
import com.easy.eats.empresa.repository.EmpresaRepository;
import com.easy.eats.mesa.enums.StatusMesa;
import com.easy.eats.mesa.model.Mesa;
import com.easy.eats.mesa.repository.MesaRepository;
import com.easy.eats.produto.enums.NaturezaProduto;
import com.easy.eats.produto.model.Produto;
import com.easy.eats.produto.repository.ProdutoRepository;
import com.easy.eats.segmento.model.Funcionalidade;
import com.easy.eats.segmento.model.Segmento;
import com.easy.eats.segmento.repository.SegmentoRepository;
import com.easy.eats.usuario.model.Role;
import com.easy.eats.usuario.model.Usuario;
import com.easy.eats.usuario.repository.UsuarioRepository;

/**
 * Cria os dados iniciais de acesso (ambiente de desenvolvimento), caso ainda
 * não existam. Em produção isso seria substituído por uma migration.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final SegmentoRepository segmentoRepository;
    private final MesaRepository mesaRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProdutoRepository produtoRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UsuarioRepository usuarioRepository, EmpresaRepository empresaRepository,
            SegmentoRepository segmentoRepository, MesaRepository mesaRepository,
            CategoriaRepository categoriaRepository, ProdutoRepository produtoRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
        this.segmentoRepository = segmentoRepository;
        this.mesaRepository = mesaRepository;
        this.categoriaRepository = categoriaRepository;
        this.produtoRepository = produtoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Segmento restaurante = segmentoSeNaoExistir("Restaurante",
                "Operação com mesas e atendimento no salão",
                EnumSet.allOf(Funcionalidade.class));

        segmentoSeNaoExistir("Food Truck",
                "Operação de rua, sem mesas fixas: pedido, cozinha, delivery e estoque",
                EnumSet.of(Funcionalidade.PEDIDO, Funcionalidade.COZINHA, Funcionalidade.DELIVERY,
                        Funcionalidade.ESTOQUE, Funcionalidade.COMPRAS, Funcionalidade.FINANCEIRO,
                        Funcionalidade.PRODUTOS, Funcionalidade.CLIENTES, Funcionalidade.USUARIOS,
                        Funcionalidade.CONFIGURACOES, Funcionalidade.CAIXA, Funcionalidade.CUPONS));

        // A empresa de demonstração usa o segmento Restaurante: ela já nasce com
        // mesas cadastradas logo abaixo, e o Food Truck não habilita OPERACAO —
        // as telas de Mesas/Comandas ficavam inacessíveis e o fluxo de comanda,
        // que é o principal do sistema, não podia ser exercitado na demo.
        Empresa empresaDemo = empresaSeNaoExistir(restaurante);
        backfillSlug(empresaDemo);
        mesasSeNaoExistir(empresaDemo, 5);
        catalogoSeNaoExistir(empresaDemo);

        // SUPERADMIN não pertence a nenhuma empresa: enxerga todas.
        criarSeNaoExistir("Super Admin", "superadmin@easyeats.com", "superadmin123", Role.SUPERADMIN, null);
        criarSeNaoExistir("Administrador", "admin@easyeats.com", "admin123", Role.ADMINISTRADOR, empresaDemo);
        criarSeNaoExistir("Operador", "operador@easyeats.com", "operador123", Role.OPERADOR, empresaDemo);
        criarSeNaoExistir("Garçom", "garcom@easyeats.com", "garcom123", Role.GARCOM, empresaDemo);
        criarSeNaoExistir("Cozinheiro", "cozinheiro@easyeats.com", "cozinheiro123", Role.COZINHEIRO, empresaDemo);
    }

    private void mesasSeNaoExistir(Empresa empresa, int quantidade) {
        List<Mesa> existentes = mesaRepository.findAllByEmpresaId(empresa.getId());
        if (!existentes.isEmpty()) {
            return;
        }

        for (int numero = 1; numero <= quantidade; numero++) {
            Mesa mesa = new Mesa();
            mesa.setNumero(numero);
            mesa.setStatus(StatusMesa.LIVRE);
            mesa.setEmpresa(empresa);
            mesaRepository.save(mesa);
        }
    }

    private void catalogoSeNaoExistir(Empresa empresa) {
        if (!produtoRepository.findAllByEmpresaId(empresa.getId()).isEmpty()) {
            return;
        }

        Categoria lanches = categoriaComProduto("Lanches", empresa);
        Categoria bebidas = categoriaComProduto("Bebidas", empresa);

        // A natureza precisa vir preenchida: é ela que faz o carrinho exibir
        // composição e adicionais (só PREPARADO/REVENDA). Sem isso os produtos
        // de exemplo abriam o modal de pedido vazio.
        produto("Hambúrguer Clássico", "Hambúrguer tradicional artesanal", new BigDecimal("22.00"), NaturezaProduto.PREPARADO, lanches,
                empresa);
        produto("X-Bacon", "Hambúrguer com bacon crocante", new BigDecimal("28.00"), NaturezaProduto.PREPARADO, lanches, empresa);
        produto("Hot Dog", "Cachorro-quente completo", new BigDecimal("15.00"), NaturezaProduto.PREPARADO, lanches, empresa);
        produto("Coca-Cola", "Refrigerante gelado 350ml", new BigDecimal("7.00"), NaturezaProduto.REVENDA, bebidas, empresa);
        produto("Água Mineral", "500ml sem gás", new BigDecimal("4.00"), NaturezaProduto.REVENDA, bebidas, empresa);
    }

    private Categoria categoriaComProduto(String nome, Empresa empresa) {
        Categoria categoria = new Categoria();
        categoria.setNome(nome);
        categoria.setFlativo(true);
        categoria.setEmpresa(empresa);
        return categoriaRepository.save(categoria);
    }

    private void produto(String nome, String descricao, BigDecimal preco, NaturezaProduto natureza, Categoria categoria,
            Empresa empresa) {
        Produto produto = new Produto();
        produto.setNome(nome);
        produto.setDescricao(descricao);
        produto.setPreco(preco);
        produto.setFlAtivo(true);
        produto.setNatureza(natureza);
        produto.setCategoria(categoria);
        produto.setEmpresa(empresa);
        produtoRepository.save(produto);
    }

    private Segmento segmentoSeNaoExistir(String nome, String descricao, Set<Funcionalidade> funcionalidades) {
        return segmentoRepository.findAll().stream()
                .filter(s -> nome.equals(s.getNome()))
                .findFirst()
                .orElseGet(() -> {
                    Segmento segmento = new Segmento();
                    segmento.setNome(nome);
                    segmento.setDescricao(descricao);
                    segmento.setFlAtivo(true);
                    segmento.setFuncionalidades(funcionalidades);
                    return segmentoRepository.save(segmento);
                });
    }

    private Empresa empresaSeNaoExistir(Segmento segmentoPadrao) {
        return empresaRepository.findAll().stream()
                .filter(e -> "Empresa Demo".equals(e.getNome()))
                .findFirst()
                .orElseGet(() -> {
                    Empresa empresa = new Empresa();
                    empresa.setNome("Empresa Demo");
                    empresa.setCnpj("00000000000000");
                    empresa.setEmail("contato@easyeats.com");
                    empresa.setFlAtivo(true);
                    empresa.setDtCriacao(LocalDateTime.now());
                    empresa.setSegmento(segmentoPadrao);
                    return empresaRepository.save(empresa);
                });
    }

    // Empresas criadas antes do campo `slug` existir ficam sem valor (ddl-auto=update
    // não faz backfill). Preenche com um slug fixo e legível na primeira subida
    // após a migração — em produção isso seria uma migration dedicada.
    private void backfillSlug(Empresa empresa) {
        if (empresa.getSlug() != null && !empresa.getSlug().isBlank()) {
            return;
        }
        empresa.setSlug("empresa-demo");
        empresaRepository.save(empresa);
    }

    private void criarSeNaoExistir(String nome, String email, String senha, Role role, Empresa empresa) {
        if (usuarioRepository.findByEmail(email).isPresent()) {
            return;
        }

        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(senha));
        usuario.setRole(role);
        usuario.setEmpresa(empresa);
        usuario.setFlAtivo(true);

        usuarioRepository.save(usuario);
    }
}
