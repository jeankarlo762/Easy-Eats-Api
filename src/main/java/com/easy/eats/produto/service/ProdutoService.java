package com.easy.eats.produto.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easy.eats.categoria.model.Categoria;
import com.easy.eats.categoria.repository.CategoriaRepository;
import com.easy.eats.empresa.repository.EmpresaRepository;
import com.easy.eats.fichatecnica.repository.FichaTecnicaItemRepository;
import com.easy.eats.produto.enums.NaturezaProduto;
import com.easy.eats.produto.model.Produto;
import com.easy.eats.produto.repository.ProdutoRepository;
import com.easy.eats.security.SecurityUtils;

@Service
public class ProdutoService {
    private final ProdutoRepository repository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private FichaTecnicaItemRepository fichaTecnicaItemRepository;

    public ProdutoService(ProdutoRepository repository){
        this.repository = repository;
    }

    public List<Produto> listarTodos() {
        if (SecurityUtils.isSuperadmin()) {
            return repository.findAll();
        }
        return repository.findAllByEmpresaId(SecurityUtils.getEmpresaId());
    }

    public Produto buscarPorId(Integer id) {
        Produto produto = SecurityUtils.isSuperadmin()
                ? repository.findById(id).orElse(null)
                : repository.findByIdAndEmpresaId(id, SecurityUtils.getEmpresaId()).orElse(null);

        if (produto == null) {
            throw new RuntimeException("Produto não encontrado");
        }
        return produto;
    }

    public Produto salvar(Produto produto) {
        produto.setId(null);
        produto.setEmpresa(empresaRepository.getReferenceById(SecurityUtils.getEmpresaId()));
        produto.setCategoria(categoriaDaMesmaEmpresa(produto.getCategoria()));
        produto.setDtAlteracao(LocalDateTime.now());
        return repository.save(produto);
    }

    public Produto atualizar(Integer id, Produto produto) {

        Produto existente = buscarPorId(id);

        if (existente.getNatureza() == NaturezaProduto.INSUMO && produto.getNatureza() != NaturezaProduto.INSUMO) {
            garantirIngredienteSemUsoEmFicha(existente);
        }

        existente.setNome(produto.getNome());
        existente.setDescricao(produto.getDescricao());
        existente.setPreco(produto.getPreco());
        existente.setCusto(produto.getCusto());
        existente.setFlAtivo(produto.getFlAtivo());
        existente.setNatureza(produto.getNatureza());
        // Telas que não lidam com unidade (ex.: catálogo) não a enviam — não apaga a já gravada.
        if (produto.getUnidadeMedida() != null) {
            existente.setUnidadeMedida(produto.getUnidadeMedida());
        }
        existente.setCategoria(categoriaDaMesmaEmpresa(produto.getCategoria()));
        existente.setDtAlteracao(LocalDateTime.now());

        return repository.save(existente);
    }

    /**
     * Desativa em vez de apagar fisicamente: Adicional, ComposicaoItem,
     * ItemCardapio e ItemVenda referenciam produto_id com NOT NULL e sem
     * cascade — assim que existir venda histórica com este produto, um
     * DELETE físico falharia por violação de constraint (ou, pior, apagaria
     * dados que relatórios futuros precisam). flAtivo já existe exatamente
     * para este caso; as listagens (ex.: novo-pedido) já filtram por ele.
     * Um ingrediente ainda usado em alguma ficha técnica não pode sair.
     */
    public void deletar(Integer id) {
        Produto produto = buscarPorId(id);
        garantirIngredienteSemUsoEmFicha(produto);
        produto.setFlAtivo(false);
        produto.setDtAlteracao(LocalDateTime.now());
        repository.save(produto);
    }

    public void garantirIngredienteSemUsoEmFicha(Produto ingrediente) {
        List<String> produtosQueUsam = fichaTecnicaItemRepository.findAllByIngredienteId(ingrediente.getId()).stream()
                .map(item -> item.getProduto().getNome())
                .distinct()
                .toList();

        if (!produtosQueUsam.isEmpty()) {
            throw new IllegalArgumentException("\"" + ingrediente.getNome() + "\" é usado na ficha técnica de: "
                    + String.join(", ", produtosQueUsam) + ". Remova-o dessas fichas antes.");
        }
    }

    private Categoria categoriaDaMesmaEmpresa(Categoria categoriaRecebida) {
        if (categoriaRecebida == null || categoriaRecebida.getId() == null) {
            return null;
        }

        Categoria categoria = SecurityUtils.isSuperadmin()
                ? categoriaRepository.findById(categoriaRecebida.getId()).orElse(null)
                : categoriaRepository.findByIdAndEmpresaId(categoriaRecebida.getId(), SecurityUtils.getEmpresaId())
                        .orElse(null);

        if (categoria == null) {
            throw new IllegalArgumentException("Categoria informada não existe ou não pertence à sua empresa");
        }
        return categoria;
    }
}
