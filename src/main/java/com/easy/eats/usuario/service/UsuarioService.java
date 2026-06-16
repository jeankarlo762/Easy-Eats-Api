package com.easy.eats.usuario.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.easy.eats.usuario.model.Usuario;
import com.easy.eats.usuario.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    public List<Usuario> listarTodos() {
        return repository.findAll();
    }

    public Usuario buscarPorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    public Usuario salvar(Usuario usuario) {
        return repository.save(usuario);
    }

    public Usuario atualizar(Integer id, Usuario usuario) {

        Usuario existente = buscarPorId(id);

        existente.setEmpresa(usuario.getEmpresa());
        existente.setNome(usuario.getNome());
        existente.setEmail(usuario.getEmail());
        existente.setSenha(usuario.getSenha());
        existente.setFlAtivo(usuario.getFlAtivo());

        return repository.save(existente);
    }

    public void deletar(Integer id) {
        repository.deleteById(id);
    }
}