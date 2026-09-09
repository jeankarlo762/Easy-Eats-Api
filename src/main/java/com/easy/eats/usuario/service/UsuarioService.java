package com.easy.eats.usuario.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.easy.eats.empresa.repository.EmpresaRepository;
import com.easy.eats.security.AuthenticatedUser;
import com.easy.eats.security.SecurityUtils;
import com.easy.eats.usuario.model.Role;
import com.easy.eats.usuario.model.Usuario;
import com.easy.eats.usuario.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    // Injeta o bean central definido em SecurityConfig em vez de instanciar um
    // BCryptPasswordEncoder próprio: os dois usam o mesmo algoritmo hoje, mas
    // uma troca futura no encoder central (ex.: aumentar o custo do BCrypt)
    // silenciosamente deixaria de valer para senhas criadas por aqui.
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private EmpresaRepository empresaRepository;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> listarTodos() {
        List<Usuario> usuarios = SecurityUtils.isSuperadmin()
                ? repository.findAll()
                : repository.findAllByEmpresaId(SecurityUtils.getEmpresaId());

        return usuarios.stream()
                .filter(usuario -> !Boolean.TRUE.equals(usuario.getFlSistema()))
                .toList();
    }

    public Usuario buscarPorId(Integer id) {
        Usuario usuario = SecurityUtils.isSuperadmin()
                ? repository.findById(id).orElse(null)
                : repository.findByIdAndEmpresaId(id, SecurityUtils.getEmpresaId()).orElse(null);

        if (usuario == null) {
            throw new RuntimeException("Usuário não encontrado");
        }
        return usuario;
    }

    public Usuario salvar(Usuario usuario) {
        if (usuario.getSenha() == null || usuario.getSenha().isBlank()) {
            throw new IllegalArgumentException("A senha é obrigatória");
        }
        if (repository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Já existe um usuário cadastrado com este e-mail");
        }

        AuthenticatedUser criador = SecurityUtils.getUsuarioAutenticado();

        usuario.setId(null);
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));

        if (criador.getRole() == Role.SUPERADMIN) {
            if (usuario.getEmpresa() == null || usuario.getEmpresa().getId() == null) {
                throw new IllegalArgumentException("Informe a empresa do usuário");
            }
            usuario.setEmpresa(empresaRepository.getReferenceById(usuario.getEmpresa().getId()));
            usuario.setRole(usuario.getRole() != null ? usuario.getRole() : Role.OPERADOR);
        } else {
            if (usuario.getRole() == Role.SUPERADMIN) {
                throw new IllegalArgumentException("Você não tem permissão para criar um usuário SUPERADMIN");
            }
            usuario.setEmpresa(empresaRepository.getReferenceById(criador.getEmpresaId()));
            usuario.setRole(usuario.getRole() != null ? usuario.getRole() : Role.OPERADOR);
        }

        return repository.save(usuario);
    }

    public Usuario atualizar(Integer id, Usuario usuario) {

        Usuario existente = buscarPorId(id);
        AuthenticatedUser autenticado = SecurityUtils.getUsuarioAutenticado();

        if (usuario.getEmail() != null && !usuario.getEmail().equalsIgnoreCase(existente.getEmail())
                && repository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Já existe um usuário cadastrado com este e-mail");
        }

        existente.setNome(usuario.getNome());
        existente.setEmail(usuario.getEmail());
        existente.setFlAtivo(usuario.getFlAtivo());

        if (usuario.getSenha() != null && !usuario.getSenha().isBlank()) {
            existente.setSenha(passwordEncoder.encode(usuario.getSenha()));
        }

        if (autenticado.getRole() == Role.SUPERADMIN) {
            if (usuario.getRole() != null) {
                existente.setRole(usuario.getRole());
            }
            if (usuario.getEmpresa() != null && usuario.getEmpresa().getId() != null) {
                existente.setEmpresa(empresaRepository.getReferenceById(usuario.getEmpresa().getId()));
            }
        } else if (usuario.getRole() != null && usuario.getRole() != Role.SUPERADMIN) {
            existente.setRole(usuario.getRole());
        }

        return repository.save(existente);
    }

    public void deletar(Integer id) {
        buscarPorId(id);
        repository.deleteById(id);
    }
}
