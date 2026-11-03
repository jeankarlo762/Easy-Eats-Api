package com.easy.eats.fichatecnica.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import com.easy.eats.fichatecnica.dto.FichaTecnicaItemPayload;
import com.easy.eats.fichatecnica.model.FichaTecnicaItem;
import com.easy.eats.fichatecnica.repository.FichaTecnicaItemRepository;
import com.easy.eats.produto.enums.NaturezaProduto;
import com.easy.eats.produto.model.Produto;
import com.easy.eats.produto.service.ProdutoService;

class FichaTecnicaItemServiceTest {

    private FichaTecnicaItemRepository repository;
    private ProdutoService produtoService;
    private FichaTecnicaItemService service;

    private Produto xBurger;
    private Produto pao;
    private Produto queijo;
    private Produto bacon;

    @BeforeEach
    void setUp() {
        repository = mock(FichaTecnicaItemRepository.class);
        produtoService = mock(ProdutoService.class);
        service = new FichaTecnicaItemService(repository);
        ReflectionTestUtils.setField(service, "produtoService", produtoService);

        xBurger = produto(1, NaturezaProduto.PREPARADO);
        pao = produto(10, NaturezaProduto.INSUMO);
        queijo = produto(11, NaturezaProduto.INSUMO);
        bacon = produto(12, NaturezaProduto.INSUMO);
        for (Produto p : List.of(xBurger, pao, queijo, bacon)) {
            when(produtoService.buscarPorId(p.getId())).thenReturn(p);
        }
    }

    @Test
    void substituirAtualizaMantidosRemoveOsQueSairamEIncluiNovos() {
        FichaTecnicaItem itemPao = item(100, pao, "2");
        FichaTecnicaItem itemQueijo = item(101, queijo, "1");
        when(repository.findAllByProdutoId(1)).thenReturn(new ArrayList<>(List.of(itemPao, itemQueijo)));

        service.substituir(1, List.of(
                new FichaTecnicaItemPayload(10, new BigDecimal("3")),
                new FichaTecnicaItemPayload(12, new BigDecimal("0.050"))));

        assertThat(itemPao.getQuantidadeUtilizada()).isEqualByComparingTo("3");
        verify(repository).deleteAll(List.of(itemQueijo));

        ArgumentCaptor<FichaTecnicaItem> salvos = ArgumentCaptor.forClass(FichaTecnicaItem.class);
        verify(repository, org.mockito.Mockito.times(2)).save(salvos.capture());
        FichaTecnicaItem novo = salvos.getAllValues().get(1);
        assertThat(novo.getId()).isNull();
        assertThat(novo.getProduto()).isSameAs(xBurger);
        assertThat(novo.getIngrediente()).isSameAs(bacon);
        assertThat(novo.getQuantidadeUtilizada()).isEqualByComparingTo("0.050");
    }

    @Test
    void substituirRecusaIngredienteRepetido() {
        assertThatThrownBy(() -> service.substituir(1, List.of(
                new FichaTecnicaItemPayload(10, BigDecimal.ONE),
                new FichaTecnicaItemPayload(10, BigDecimal.TEN))))
                .isInstanceOf(IllegalArgumentException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void substituirRecusaIngredienteQueNaoEhInsumo() {
        when(repository.findAllByProdutoId(1)).thenReturn(new ArrayList<>());
        Produto refrigerante = produto(20, NaturezaProduto.REVENDA);
        when(produtoService.buscarPorId(20)).thenReturn(refrigerante);

        assertThatThrownBy(() -> service.substituir(1, List.of(new FichaTecnicaItemPayload(20, BigDecimal.ONE))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("INSUMO");
    }

    @Test
    void substituirRecusaQuantidadeNaoPositiva() {
        assertThatThrownBy(() -> service.substituir(1, List.of(new FichaTecnicaItemPayload(10, BigDecimal.ZERO))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void salvarRecusaIngredienteJaPresenteNaFicha() {
        when(repository.existsByProdutoIdAndIngredienteId(1, 10)).thenReturn(true);

        assertThatThrownBy(() -> service.salvar(1, 10, BigDecimal.ONE))
                .isInstanceOf(IllegalArgumentException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void custoTotalSomaCustoDoIngredienteVezesQuantidadeUtilizada() {
        pao.setCusto(new BigDecimal("1.50"));
        queijo.setCusto(new BigDecimal("0.04"));
        when(repository.findAllByProdutoIdOrderByIdAsc(1)).thenReturn(List.of(item(100, pao, "2"), item(101, queijo, "50")));

        assertThat(service.custoTotal(1)).isEqualByComparingTo("5.00");
    }

    private Produto produto(int id, NaturezaProduto natureza) {
        Produto produto = new Produto();
        produto.setId(id);
        produto.setNome("Produto " + id);
        produto.setNatureza(natureza);
        produto.setFlAtivo(true);
        return produto;
    }

    private FichaTecnicaItem item(int id, Produto ingrediente, String quantidade) {
        FichaTecnicaItem item = new FichaTecnicaItem();
        item.setId(id);
        item.setProduto(xBurger);
        item.setIngrediente(ingrediente);
        item.setQuantidadeUtilizada(new BigDecimal(quantidade));
        return item;
    }
}
