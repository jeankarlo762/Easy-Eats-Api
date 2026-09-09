package com.easy.eats.cliente.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easy.eats.cliente.model.Cliente;
import com.easy.eats.cliente.repository.ClienteRepository;
import com.easy.eats.empresa.repository.EmpresaRepository;
import com.easy.eats.security.SecurityUtils;

@Service
public class ClienteService {

    @Autowired
    ClienteRepository repository;

    @Autowired
    EmpresaRepository empresaRepository;

    public Cliente criar(Cliente cliente) {
        cliente.setId(null);
        cliente.setEmpresa(empresaRepository.getReferenceById(SecurityUtils.getEmpresaId()));
        // Nada preenchia essas datas — todo cliente nascia sem registro de
        // quando foi cadastrado (mesmo bug já corrigido em Venda.dt_criacao).
        cliente.setDt_criacao(LocalDateTime.now().toString());
        cliente.setDt_alteracao(LocalDateTime.now().toString());
        return repository.save(cliente);
    }

    public Cliente salvar(Cliente cliente) {
        cliente.setDt_alteracao(LocalDateTime.now().toString());
        return repository.save(cliente);
    }

    public List<Cliente> listarTodos() {
        if (SecurityUtils.isSuperadmin()) {
            return repository.findAll();
        }
        return repository.findAllByEmpresaId(SecurityUtils.getEmpresaId());
    }

    public Optional<Cliente> buscarPorId(Integer id) {
        if (SecurityUtils.isSuperadmin()) {
            return repository.findById(id);
        }
        return repository.findByIdAndEmpresaId(id, SecurityUtils.getEmpresaId());
    }

    public void deletar(Integer id) {
        if (buscarPorId(id).isEmpty()) {
            return;
        }
        repository.deleteById(id);
    }
}
