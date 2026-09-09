package com.easy.eats.integracao.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easy.eats.empresa.repository.EmpresaRepository;
import com.easy.eats.integracao.model.IntegracaoConfig;
import com.easy.eats.integracao.repository.IntegracaoConfigRepository;
import com.easy.eats.security.SecurityUtils;

@Service
public class IntegracaoConfigService {

    @Autowired
    private IntegracaoConfigRepository repository;

    @Autowired
    private EmpresaRepository empresaRepository;

    public List<IntegracaoConfig> listar() {
        return repository.findAllByEmpresaId(SecurityUtils.getEmpresaId());
    }

    public IntegracaoConfig salvar(String chave, String credenciaisJson, Boolean ativo) {
        Integer empresaId = SecurityUtils.getEmpresaId();

        IntegracaoConfig config = repository.findByEmpresaIdAndChave(empresaId, chave).orElseGet(() -> {
            IntegracaoConfig novo = new IntegracaoConfig();
            novo.setChave(chave);
            novo.setEmpresa(empresaRepository.getReferenceById(empresaId));
            return novo;
        });

        config.setCredenciaisJson(credenciaisJson);
        config.setAtivo(ativo != null && ativo);

        return repository.save(config);
    }
}
