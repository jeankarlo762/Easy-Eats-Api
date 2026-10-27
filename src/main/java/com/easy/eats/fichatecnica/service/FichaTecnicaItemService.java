package com.easy.eats.fichatecnica.service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.easy.eats.estoque.repository.EstoqueRepository;
import com.easy.eats.fichatecnica.dto.FichaTecnicaItemPayload;
import com.easy.eats.fichatecnica.model.FichaTecnicaItem;
import com.easy.eats.fichatecnica.repository.FichaTecnicaItemRepository;
import com.easy.eats.produto.enums.NaturezaProduto;
import com.easy.eats.produto.model.Produto;
import com.easy.eats.produto.service.ProdutoService;

@Service
public class FichaTecnicaItemService {

    private final FichaTecnicaItemRepository repository;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private EstoqueRepository estoqueRepository;

    public FichaTecnicaItemService(FichaTecnicaItemRepository repository) {
        this.repository = repository;
    }

    public List<FichaTecnicaItem> listarPorProduto(Integer produtoId) {
        produtoService.buscarPorId(produtoId);
        return repository.findAllByProdutoId(produtoId);
    }

    public FichaTecnicaItem salvar(Integer produtoId, Integer ingredienteId, BigDecimal quantidadeUtilizada) {
        Produto produto = produtoComFicha(produtoId);
        Produto ingrediente = ingredienteDoEstoque(produto, ingredienteId);

        if (repository.existsByProdutoIdAndIngredienteId(produtoId, ingredienteId)) {
            throw new IllegalArgumentException("Este ingrediente já está na ficha técnica; edite a quantidade utilizada");
        }

        FichaTecnicaItem item = new FichaTecnicaItem();
        item.setProduto(produto);
        item.setIngrediente(ingrediente);
        item.setQuantidadeUtilizada(quantidadeUtilizada);

        return repository.save(item);
    }

    public FichaTecnicaItem atualizar(Integer produtoId, Integer id, BigDecimal quantidadeUtilizada) {
        FichaTecnicaItem existente = buscarPorIdEProduto(produtoId, id);
        existente.setQuantidadeUtilizada(quantidadeUtilizada);
        return repository.save(existente);
    }

    public void deletar(Integer produtoId, Integer id) {
        buscarPorIdEProduto(produtoId, id);
        repository.deleteById(id);
    }

    /**
     * Grava a composição completa do produto de uma vez (usado ao salvar o
     * produto): atualiza a quantidade dos ingredientes que continuam, insere
     * os novos e remove os que saíram — tudo ou nada.
     */
    @Transactional
    public List<FichaTecnicaItem> substituir(Integer produtoId, List<FichaTecnicaItemPayload> itens) {
        List<FichaTecnicaItemPayload> recebidos = itens != null ? itens : List.of();
        Produto produto = recebidos.isEmpty() ? produtoService.buscarPorId(produtoId) : produtoComFicha(produtoId);

        Set<Integer> ingredientesVistos = new HashSet<>();
        for (FichaTecnicaItemPayload item : recebidos) {
            if (item == null || item.ingredienteId() == null) {
                throw new IllegalArgumentException("O ingrediente é obrigatório");
            }
            if (item.quantidadeUtilizada() == null || item.quantidadeUtilizada().signum() <= 0) {
                throw new IllegalArgumentException("A quantidade utilizada deve ser maior que zero");
            }
            if (!ingredientesVistos.add(item.ingredienteId())) {
                throw new IllegalArgumentException("O mesmo ingrediente foi informado mais de uma vez na ficha técnica");
            }
        }

        Map<Integer, FichaTecnicaItem> existentesPorIngrediente = repository.findAllByProdutoId(produtoId).stream()
                .collect(Collectors.toMap(FichaTecnicaItem::getIngredienteId, Function.identity()));

        for (FichaTecnicaItemPayload item : recebidos) {
            FichaTecnicaItem fichaItem = existentesPorIngrediente.remove(item.ingredienteId());
            if (fichaItem == null) {
                fichaItem = new FichaTecnicaItem();
                fichaItem.setProduto(produto);
                fichaItem.setIngrediente(ingredienteDoEstoque(produto, item.ingredienteId()));
            }
            fichaItem.setQuantidadeUtilizada(item.quantidadeUtilizada());
            repository.save(fichaItem);
        }

        repository.deleteAll(existentesPorIngrediente.values());
        repository.flush();

        return repository.findAllByProdutoId(produtoId);
    }

    /** Custo de produção de uma unidade do produto: soma de custo do ingrediente × quantidade utilizada. */
    public BigDecimal custoTotal(Integer produtoId) {
        return listarPorProduto(produtoId).stream()
                .map(item -> custoUnitario(item.getIngrediente()).multiply(item.getQuantidadeUtilizada()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Ingredientes antigos (e os do seed) guardavam o preço de compra em
     * "preco" e deixavam "custo" vazio — para um INSUMO os dois significam o
     * mesmo, então cai para o preço quando não há custo.
     */
    private BigDecimal custoUnitario(Produto ingrediente) {
        if (ingrediente.getCusto() != null) {
            return ingrediente.getCusto();
        }
        return ingrediente.getPreco() != null ? ingrediente.getPreco() : BigDecimal.ZERO;
    }

    private Produto produtoComFicha(Integer produtoId) {
        Produto produto = produtoService.buscarPorId(produtoId);
        if (produto.getNatureza() != NaturezaProduto.PREPARADO) {
            throw new IllegalArgumentException("Só produtos de natureza PREPARADO têm ficha técnica");
        }
        return produto;
    }

    /** O ingrediente precisa ser um INSUMO ativo da empresa e já estar cadastrado no estoque. */
    private Produto ingredienteDoEstoque(Produto produto, Integer ingredienteId) {
        if (ingredienteId.equals(produto.getId())) {
            throw new IllegalArgumentException("Um produto não pode ser ingrediente de si mesmo");
        }

        Produto ingrediente = produtoService.buscarPorId(ingredienteId);
        if (ingrediente.getNatureza() != NaturezaProduto.INSUMO) {
            throw new IllegalArgumentException("O produto informado como ingrediente não tem natureza INSUMO");
        }
        if (Boolean.FALSE.equals(ingrediente.getFlAtivo())) {
            throw new IllegalArgumentException("O ingrediente \"" + ingrediente.getNome() + "\" está inativo");
        }
        if (estoqueRepository.findByProdutoId(ingredienteId).isEmpty()) {
            throw new IllegalArgumentException(
                    "O ingrediente \"" + ingrediente.getNome() + "\" não está cadastrado no estoque");
        }
        return ingrediente;
    }

    private FichaTecnicaItem buscarPorIdEProduto(Integer produtoId, Integer id) {
        produtoService.buscarPorId(produtoId);
        return repository.findByIdAndProdutoId(id, produtoId)
                .orElseThrow(() -> new RuntimeException("Item de ficha técnica não encontrado"));
    }
}
