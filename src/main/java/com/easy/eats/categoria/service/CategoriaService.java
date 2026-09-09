package com.easy.eats.categoria.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easy.eats.categoria.model.Categoria;
import com.easy.eats.categoria.repository.CategoriaRepository;
import com.easy.eats.empresa.repository.EmpresaRepository;
import com.easy.eats.produto.repository.ProdutoRepository;
import com.easy.eats.security.SecurityUtils;

@Service
public class CategoriaService {

    private final CategoriaRepository repository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    public CategoriaService(CategoriaRepository repository) {
        this.repository = repository;
    }

    public List<Categoria> listarTodos() {
        if (SecurityUtils.isSuperadmin()) {
            return repository.findAll();
        }
        return repository.findAllByEmpresaId(SecurityUtils.getEmpresaId());
    }

    public Categoria buscarPorId(Integer id) {
        Categoria categoria = SecurityUtils.isSuperadmin()
                ? repository.findById(id).orElse(null)
                : repository.findByIdAndEmpresaId(id, SecurityUtils.getEmpresaId()).orElse(null);

        if (categoria == null) {
            throw new RuntimeException("Categoria não encontrada");
        }
        return categoria;
    }

    public Categoria salvar(Categoria categoria) {
        categoria.setId(null);
        categoria.setEmpresa(empresaRepository.getReferenceById(SecurityUtils.getEmpresaId()));
        if (categoria.getFlativo() == null) {
            categoria.setFlativo(true);
        }
        return repository.save(categoria);
    }

    public Categoria atualizar(Integer id, Categoria categoria) {

        Categoria categoriaExistente = buscarPorId(id);

        categoriaExistente.setNome(categoria.getNome());
        categoriaExistente.setDescricao(categoria.getDescricao());
        categoriaExistente.setFlativo(categoria.getFlativo());
        categoriaExistente.setDt_alteracao(categoria.getDt_alteracao());

        return repository.save(categoriaExistente);
    }

    /**
     * Categoria.produto é @ManyToOne sem nullable=false: sem esta checagem, um
     * DELETE aqui não falhava — apagava a categoria e deixava categoria_id NULL
     * nos produtos vinculados, desassociando-os silenciosamente. Bloquear com
     * uma mensagem clara é mais seguro do que depender do comportamento
     * default da FK opcional.
     */
    public void deletar(Integer id) {
        buscarPorId(id);
        if (produtoRepository.existsByCategoriaId(id)) {
            throw new IllegalArgumentException(
                    "Não é possível excluir esta categoria: há produtos vinculados a ela.");
        }
        repository.deleteById(id);
    }
}
