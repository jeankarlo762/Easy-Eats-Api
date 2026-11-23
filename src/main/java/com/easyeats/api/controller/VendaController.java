package com.easyeats.api.controller;

import com.easyeats.api.dto.AberturaVendaDto;
import com.easyeats.api.dto.AtualizacaoVendaDto;
import com.easyeats.api.dto.CancelamentoVendaDto;
import com.easyeats.api.dto.FiltroVendaDto;
import com.easyeats.api.dto.OcupacaoMesaDto;
import com.easyeats.api.dto.PagamentoVendaDto;
import com.easyeats.api.dto.PaginaDto;
import com.easyeats.api.dto.ResumoVendasDto;
import com.easyeats.api.dto.VendaDto;
import com.easyeats.api.dto.VendaResumoDto;
import com.easyeats.api.entity.StatusVendaEnum;
import com.easyeats.api.security.UsuarioAutenticado;
import com.easyeats.api.service.VendaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/vendas")
@RequiredArgsConstructor
@Tag(name = "Vendas", description = "A venda é a conta de uma mesa ou de uma retirada e agrupa as comandas. A mesa fica ocupada enquanto houver venda aberta nela e só libera quando a venda é finalizada ou cancelada. Para finalizar, os pagamentos precisam somar exatamente o total e o caixa precisa estar aberto.")
@SecurityRequirement(name = "bearerAuth")
public class VendaController {

    private final VendaService vendaService;

    @Operation(summary = "Abrir venda", description = "Abre uma venda sem lançar comanda. Sem o tipo informado, vale MESA quando há mesaId e RETIRADA quando não há. MESA exige a mesa e não aceita mesa que já tenha venda aberta. RETIRADA exige o nome do cliente e não usa mesa. DELIVERY ainda não está disponível. Ao lançar uma comanda em Pedidos, a venda da mesa também é aberta automaticamente.")
    @ApiResponse(responseCode = "201", description = "Venda aberta com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "404", description = "Mesa não encontrada")
    @ApiResponse(responseCode = "422", description = "Mesa não informada ou já ocupada, nome do cliente da retirada ausente ou delivery indisponível")
    @PostMapping
    public ResponseEntity<VendaDto> abrir(@Valid @RequestBody AberturaVendaDto dto, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(vendaService.abrir(dto, UsuarioAutenticado.de(authentication)));
    }

    @Operation(summary = "Listar vendas (histórico)", description = "Histórico paginado de vendas, com filtros opcionais por status, mesa, usuário e período. Restrito a Administrador e Gerente.")
    @ApiResponse(responseCode = "200", description = "Página de vendas retornada com sucesso")
    @ApiResponse(responseCode = "403", description = "Operação permitida apenas para Administrador ou Gerente")
    @GetMapping
    public ResponseEntity<PaginaDto<VendaResumoDto>> listar(
            @Parameter(description = "Filtra pelo status da venda: ABERTA, FINALIZADA ou CANCELADA", example = "FINALIZADA") @RequestParam(required = false) StatusVendaEnum status,
            @Parameter(description = "Filtra pelas vendas de uma mesa", example = "4") @RequestParam(required = false) Long mesaId,
            @Parameter(description = "Filtra pelo usuário que abriu a venda", example = "superadmin") @RequestParam(required = false) String usuario,
            @Parameter(description = "Data inicial do período, inclusive (AAAA-MM-DD)", example = "2026-03-01") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @Parameter(description = "Data final do período, inclusive (AAAA-MM-DD)", example = "2026-03-31") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @Parameter(description = "Número da página, começando em 0", example = "0") @RequestParam(defaultValue = "0") int pagina,
            @Parameter(description = "Quantidade de itens por página", example = "20") @RequestParam(defaultValue = "20") int tamanho,
            Authentication authentication) {
        FiltroVendaDto filtro = new FiltroVendaDto(status, mesaId, usuario, inicio, fim);
        return ResponseEntity.ok(vendaService.listar(filtro, pagina, tamanho, UsuarioAutenticado.de(authentication)));
    }

    @Operation(summary = "Listar vendas abertas", description = "Retorna as vendas que ainda estão abertas, de mesa e de retirada. Disponível para qualquer usuário autenticado.")
    @ApiResponse(responseCode = "200", description = "Lista de vendas abertas retornada com sucesso")
    @GetMapping("/abertas")
    public ResponseEntity<List<VendaResumoDto>> listarAbertas() {
        return ResponseEntity.ok(vendaService.listarAbertas());
    }

    @Operation(summary = "Resumo de faturamento", description = "Faturamento do período: total faturado, quantidade de vendas, ticket médio, vendas canceladas e totais por forma de pagamento e por dia. Conta as vendas finalizadas pela data de fechamento. Restrito a Administrador e Gerente.")
    @ApiResponse(responseCode = "200", description = "Resumo retornado com sucesso")
    @ApiResponse(responseCode = "403", description = "Operação permitida apenas para Administrador ou Gerente")
    @GetMapping("/resumo")
    public ResponseEntity<ResumoVendasDto> resumir(
            @Parameter(description = "Data inicial do período, inclusive (AAAA-MM-DD)", example = "2026-03-01") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @Parameter(description = "Data final do período, inclusive (AAAA-MM-DD)", example = "2026-03-31") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            Authentication authentication) {
        return ResponseEntity.ok(vendaService.resumir(inicio, fim, UsuarioAutenticado.de(authentication)));
    }

    @Operation(summary = "Ocupação das mesas", description = "Retorna cada mesa ativa com a situação atual: livre ou ocupada, e, quando ocupada, a venda, o cliente, a quantidade de comandas e o valor parcial.")
    @ApiResponse(responseCode = "200", description = "Ocupação das mesas retornada com sucesso")
    @GetMapping("/ocupacao-mesas")
    public ResponseEntity<List<OcupacaoMesaDto>> ocupacaoDasMesas() {
        return ResponseEntity.ok(vendaService.ocupacaoDasMesas());
    }

    @Operation(summary = "Buscar a venda aberta de uma mesa", description = "Retorna a venda aberta da mesa informada, com comandas e pagamentos.")
    @ApiResponse(responseCode = "200", description = "Venda aberta encontrada")
    @ApiResponse(responseCode = "204", description = "A mesa está livre, sem venda aberta")
    @GetMapping("/mesa/{mesaId}/aberta")
    public ResponseEntity<VendaDto> buscarAbertaDaMesa(@PathVariable Long mesaId) {
        return vendaService.buscarAbertaDaMesa(mesaId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @Operation(summary = "Buscar venda por ID", description = "Retorna a venda com as comandas, os pagamentos, o total, o valor pago e o valor restante.")
    @ApiResponse(responseCode = "200", description = "Venda encontrada")
    @ApiResponse(responseCode = "404", description = "Venda não encontrada")
    @GetMapping("/{id}")
    public ResponseEntity<VendaDto> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(vendaService.buscarPorId(id));
    }

    @Operation(summary = "Alterar nome do cliente e observação", description = "Altera o nome do cliente e a observação de uma venda aberta.")
    @ApiResponse(responseCode = "200", description = "Venda alterada com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "404", description = "Venda não encontrada")
    @ApiResponse(responseCode = "422", description = "A venda não está aberta")
    @PatchMapping("/{id}")
    public ResponseEntity<VendaDto> atualizar(@PathVariable Long id, @Valid @RequestBody AtualizacaoVendaDto dto) {
        return ResponseEntity.ok(vendaService.atualizar(id, dto));
    }

    @Operation(summary = "Registrar pagamento", description = "Registra um pagamento parcial ou total em uma venda aberta. O valor não pode passar do que falta pagar. Em dinheiro, informe quanto o cliente entregou em valorRecebido (o troco é calculado) e, se omitido, vale o próprio valor. Nenhum valor entra no caixa antes de finalizar a venda.")
    @ApiResponse(responseCode = "201", description = "Pagamento registrado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos (forma e valor obrigatórios, até 2 casas decimais)")
    @ApiResponse(responseCode = "404", description = "Venda não encontrada")
    @ApiResponse(responseCode = "422", description = "Venda não está aberta, pagamento maior que o valor restante ou valor recebido menor que o pagamento")
    @PostMapping("/{id}/pagamentos")
    public ResponseEntity<VendaDto> registrarPagamento(@PathVariable Long id,
            @Valid @RequestBody PagamentoVendaDto dto, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(vendaService.registrarPagamento(id, dto, UsuarioAutenticado.de(authentication)));
    }

    @Operation(summary = "Remover pagamento", description = "Remove um pagamento de uma venda aberta. Como nada entra no caixa antes de finalizar, não há estorno.")
    @ApiResponse(responseCode = "200", description = "Pagamento removido com sucesso")
    @ApiResponse(responseCode = "404", description = "Venda ou pagamento não encontrado")
    @ApiResponse(responseCode = "422", description = "A venda não está aberta")
    @DeleteMapping("/{id}/pagamentos/{pagamentoId}")
    public ResponseEntity<VendaDto> removerPagamento(@PathVariable Long id, @PathVariable Long pagamentoId) {
        return ResponseEntity.ok(vendaService.removerPagamento(id, pagamentoId));
    }

    @Operation(summary = "Finalizar venda", description = "Exige que os pagamentos somem exatamente o total da venda e que haja caixa aberto. Libera a mesa e lança no caixa o dinheiro recebido (a parte paga em dinheiro, já sem o troco).")
    @ApiResponse(responseCode = "200", description = "Venda finalizada com sucesso")
    @ApiResponse(responseCode = "404", description = "Venda não encontrada")
    @ApiResponse(responseCode = "422", description = "Venda sem comandas, valor pago diferente do total, caixa fechado ou venda não aberta")
    @PostMapping("/{id}/finalizar")
    public ResponseEntity<VendaDto> finalizar(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(vendaService.finalizar(id, UsuarioAutenticado.de(authentication)));
    }

    @Operation(summary = "Cancelar venda", description = "Cancela a venda e todas as comandas dela. O estoque não volta por padrão. Com devolverEstoque verdadeiro e a senha do gerente, os ingredientes voltam ao estoque nas comandas que já baixaram o estoque e ainda estavam em ABERTO ou EM_PREPARO; comandas prontas nunca devolvem. Restrito a Administrador e Gerente.")
    @ApiResponse(responseCode = "200", description = "Venda cancelada com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos (o motivo é obrigatório)")
    @ApiResponse(responseCode = "403", description = "Operação permitida apenas para Administrador ou Gerente")
    @ApiResponse(responseCode = "404", description = "Venda não encontrada")
    @ApiResponse(responseCode = "422", description = "Venda não está aberta, senha do gerente ausente ou incorreta")
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<VendaDto> cancelar(@PathVariable Long id,
            @Valid @RequestBody CancelamentoVendaDto dto, Authentication authentication) {
        return ResponseEntity.ok(vendaService.cancelar(id, dto, UsuarioAutenticado.de(authentication)));
    }
}
