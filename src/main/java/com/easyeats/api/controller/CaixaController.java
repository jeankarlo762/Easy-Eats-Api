package com.easyeats.api.controller;

import com.easyeats.api.dto.AberturaCaixaDto;
import com.easyeats.api.dto.CaixaDto;
import com.easyeats.api.dto.FechamentoCaixaDto;
import com.easyeats.api.dto.FiltroLancamentosDto;
import com.easyeats.api.dto.MovimentacaoCaixaDto;
import com.easyeats.api.dto.PaginaDto;
import com.easyeats.api.dto.RelatorioFechamentoDto;
import com.easyeats.api.dto.ResumoLancamentosDto;
import com.easyeats.api.entity.StatusCaixaEnum;
import com.easyeats.api.entity.TipoMovimentacaoCaixaEnum;
import com.easyeats.api.security.UsuarioAutenticado;
import com.easyeats.api.service.CaixaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/caixa")
@RequiredArgsConstructor
@Tag(name = "Caixa", description = "Abertura, lançamentos e fechamento do caixa. Só pode haver um caixa aberto por vez e ele é obrigatório para finalizar vendas. Administrador e Gerente fazem tudo; o Funcionário abre o caixa, consulta o caixa atual e lança suprimento.")
@SecurityRequirement(name = "bearerAuth")
public class CaixaController {

    private final CaixaService caixaService;

    @Operation(summary = "Abrir caixa", description = "Abre o caixa com o fundo inicial informado e lança o fundo como entrada. Qualquer usuário autenticado pode abrir, desde que não haja outro caixa aberto.")
    @ApiResponse(responseCode = "201", description = "Caixa aberto com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos (fundo obrigatório, não negativo, até 2 casas decimais)")
    @ApiResponse(responseCode = "422", description = "Já existe um caixa aberto")
    @PostMapping("/abrir")
    public ResponseEntity<CaixaDto> abrir(@Valid @RequestBody AberturaCaixaDto dto,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(caixaService.abrir(dto, UsuarioAutenticado.de(authentication)));
    }

    @Operation(summary = "Consultar o caixa aberto", description = "Retorna o caixa aberto no momento, com o saldo atual (fundo + suprimentos - sangrias + vendas em dinheiro). Disponível para qualquer usuário autenticado.")
    @ApiResponse(responseCode = "200", description = "Caixa aberto retornado com sucesso")
    @ApiResponse(responseCode = "204", description = "Não há caixa aberto")
    @GetMapping("/atual")
    public ResponseEntity<CaixaDto> buscarAtual() {
        return caixaService.buscarAtual()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @Operation(summary = "Listar caixas (histórico)", description = "Histórico paginado de caixas, com filtros opcionais por período, usuário e status. Restrito a Administrador e Gerente.")
    @ApiResponse(responseCode = "200", description = "Página de caixas retornada com sucesso")
    @ApiResponse(responseCode = "403", description = "Operação permitida apenas para Administrador ou Gerente")
    @GetMapping
    public ResponseEntity<PaginaDto<CaixaDto>> listar(
            @Parameter(description = "Data inicial do período, inclusive (AAAA-MM-DD)", example = "2026-03-01") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @Parameter(description = "Data final do período, inclusive (AAAA-MM-DD)", example = "2026-03-31") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @Parameter(description = "Filtra pelo nome de usuário", example = "superadmin") @RequestParam(required = false) String usuario,
            @Parameter(description = "Filtra pelo status do caixa: ABERTO ou FECHADO", example = "FECHADO") @RequestParam(required = false) StatusCaixaEnum status,
            @Parameter(description = "Número da página, começando em 0", example = "0") @RequestParam(defaultValue = "0") int pagina,
            @Parameter(description = "Quantidade de itens por página", example = "20") @RequestParam(defaultValue = "20") int tamanho,
            Authentication authentication) {
        return ResponseEntity.ok(caixaService.listar(inicio, fim, usuario, status, pagina, tamanho,
                UsuarioAutenticado.de(authentication)));
    }

    @Operation(summary = "Buscar caixa por ID", description = "Retorna os dados de um caixa, aberto ou fechado. Restrito a Administrador e Gerente.")
    @ApiResponse(responseCode = "200", description = "Caixa encontrado")
    @ApiResponse(responseCode = "403", description = "Operação permitida apenas para Administrador ou Gerente")
    @ApiResponse(responseCode = "404", description = "Caixa não encontrado")
    @GetMapping("/{id}")
    public ResponseEntity<CaixaDto> buscarPorId(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(caixaService.buscarPorId(id, UsuarioAutenticado.de(authentication)));
    }

    @Operation(summary = "Lançar suprimento ou sangria", description = "SUPRIMENTO (entrada de dinheiro) pode ser lançado por qualquer usuário. SANGRIA (retirada) é restrita a Administrador e Gerente, exige descrição com o motivo e não pode passar do saldo. O fundo inicial só entra na abertura e as vendas só entram ao finalizar a venda.")
    @ApiResponse(responseCode = "201", description = "Lançamento registrado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos (valor maior que zero, até 2 casas decimais)")
    @ApiResponse(responseCode = "403", description = "Sangria permitida apenas para Administrador ou Gerente")
    @ApiResponse(responseCode = "404", description = "Caixa não encontrado")
    @ApiResponse(responseCode = "422", description = "Caixa fechado, tipo não permitido, motivo da sangria ausente ou saldo insuficiente")
    @PostMapping("/{id}/movimentacoes")
    public ResponseEntity<MovimentacaoCaixaDto> registrarMovimentacao(@PathVariable Long id,
            @Valid @RequestBody MovimentacaoCaixaDto dto, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(caixaService.registrarMovimentacao(id, dto, UsuarioAutenticado.de(authentication)));
    }

    @Operation(summary = "Fechar caixa", description = "Fecha o caixa com o valor contado. O sistema compara com o valor esperado e, se houver diferença, a justificativa na observação é obrigatória. Restrito a Administrador e Gerente.")
    @ApiResponse(responseCode = "200", description = "Caixa fechado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "403", description = "Operação permitida apenas para Administrador ou Gerente")
    @ApiResponse(responseCode = "404", description = "Caixa não encontrado")
    @ApiResponse(responseCode = "422", description = "Caixa já fechado ou justificativa da diferença ausente")
    @PostMapping("/{id}/fechar")
    public ResponseEntity<CaixaDto> fechar(@PathVariable Long id,
            @Valid @RequestBody FechamentoCaixaDto dto, Authentication authentication) {
        return ResponseEntity.ok(caixaService.fechar(id, dto, UsuarioAutenticado.de(authentication)));
    }

    @Operation(summary = "Relatório de fechamento do caixa", description = "Resumo de um caixa já fechado: totais de suprimentos, sangrias e vendas e a lista de lançamentos. Restrito a Administrador e Gerente.")
    @ApiResponse(responseCode = "200", description = "Relatório retornado com sucesso")
    @ApiResponse(responseCode = "403", description = "Operação permitida apenas para Administrador ou Gerente")
    @ApiResponse(responseCode = "404", description = "Caixa não encontrado")
    @ApiResponse(responseCode = "422", description = "O relatório só está disponível após o fechamento do caixa")
    @GetMapping("/{id}/relatorio-fechamento")
    public ResponseEntity<RelatorioFechamentoDto> relatorioFechamento(@PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(
                caixaService.gerarRelatorioFechamento(id, UsuarioAutenticado.de(authentication)));
    }

    @Operation(summary = "Listar lançamentos do caixa", description = "Lançamentos paginados (fundo inicial, suprimentos, sangrias e vendas em dinheiro), com filtros opcionais. Restrito a Administrador e Gerente.")
    @ApiResponse(responseCode = "200", description = "Página de lançamentos retornada com sucesso")
    @ApiResponse(responseCode = "403", description = "Operação permitida apenas para Administrador ou Gerente")
    @GetMapping("/movimentacoes")
    public ResponseEntity<PaginaDto<MovimentacaoCaixaDto>> listarMovimentacoes(
            @Parameter(description = "Data inicial do período, inclusive (AAAA-MM-DD)", example = "2026-03-01") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @Parameter(description = "Data final do período, inclusive (AAAA-MM-DD)", example = "2026-03-31") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @Parameter(description = "Filtra pelo tipo do lançamento: FUNDO_INICIAL, SUPRIMENTO, SANGRIA ou VENDA", example = "SANGRIA") @RequestParam(required = false) TipoMovimentacaoCaixaEnum tipo,
            @Parameter(description = "Filtra pelo nome de usuário", example = "superadmin") @RequestParam(required = false) String usuario,
            @Parameter(description = "Filtra pelos lançamentos de um caixa", example = "1") @RequestParam(required = false) Long caixaId,
            @Parameter(description = "Número da página, começando em 0", example = "0") @RequestParam(defaultValue = "0") int pagina,
            @Parameter(description = "Quantidade de itens por página", example = "20") @RequestParam(defaultValue = "20") int tamanho,
            Authentication authentication) {
        FiltroLancamentosDto filtro = new FiltroLancamentosDto(inicio, fim, tipo, usuario, caixaId);
        return ResponseEntity.ok(caixaService.listarMovimentacoes(filtro, pagina, tamanho,
                UsuarioAutenticado.de(authentication)));
    }

    @Operation(summary = "Resumir lançamentos do caixa", description = "Totais de entradas, saídas e saldo dos lançamentos filtrados, sem depender da página. Restrito a Administrador e Gerente.")
    @ApiResponse(responseCode = "200", description = "Resumo retornado com sucesso")
    @ApiResponse(responseCode = "403", description = "Operação permitida apenas para Administrador ou Gerente")
    @GetMapping("/movimentacoes/resumo")
    public ResponseEntity<ResumoLancamentosDto> resumirMovimentacoes(
            @Parameter(description = "Data inicial do período, inclusive (AAAA-MM-DD)", example = "2026-03-01") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @Parameter(description = "Data final do período, inclusive (AAAA-MM-DD)", example = "2026-03-31") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @Parameter(description = "Filtra pelo tipo do lançamento: FUNDO_INICIAL, SUPRIMENTO, SANGRIA ou VENDA", example = "SANGRIA") @RequestParam(required = false) TipoMovimentacaoCaixaEnum tipo,
            @Parameter(description = "Filtra pelo nome de usuário", example = "superadmin") @RequestParam(required = false) String usuario,
            @Parameter(description = "Filtra pelos lançamentos de um caixa", example = "1") @RequestParam(required = false) Long caixaId,
            Authentication authentication) {
        FiltroLancamentosDto filtro = new FiltroLancamentosDto(inicio, fim, tipo, usuario, caixaId);
        return ResponseEntity.ok(caixaService.resumirMovimentacoes(filtro,
                UsuarioAutenticado.de(authentication)));
    }
}
