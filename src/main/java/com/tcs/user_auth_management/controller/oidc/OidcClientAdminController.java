package com.tcs.user_auth_management.controller.oidc;

import com.tcs.user_auth_management.model.dto.oidc.DtoOidcClientRegister;
import com.tcs.user_auth_management.model.dto.oidc.DtoOidcClientResponse;
import com.tcs.user_auth_management.service.oidc.OidcClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/oidc/clients")
@AllArgsConstructor
@Tag(name = "OIDC Client Administration", description = "Admin endpoints to register and manage OIDC client applications")
@SecurityRequirement(name = "bearerAuth")
public class OidcClientAdminController {
  private final OidcClientService clientService;

  @PostMapping
  @Operation(summary = "Register OIDC client", description = "Creates a new client application registration")
  public ResponseEntity<DtoOidcClientResponse> registerClient(@Valid @RequestBody DtoOidcClientRegister register) {
    return ResponseEntity.status(HttpStatus.CREATED).body(clientService.registerClient(register));
  }

  @GetMapping
  @Operation(summary = "List OIDC clients", description = "Returns all registered OIDC clients")
  public ResponseEntity<List<DtoOidcClientResponse>> listClients() {
    return ResponseEntity.ok(clientService.getAllClients());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get OIDC client details", description = "Returns details for a single client")
  public ResponseEntity<DtoOidcClientResponse> getClient(@PathVariable UUID id) {
    return ResponseEntity.ok(clientService.getClientDetails(id));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Disable OIDC client", description = "Disables an OIDC client")
  public ResponseEntity<Void> disableClient(@PathVariable UUID id) {
    clientService.disableClient(id);
    return ResponseEntity.noContent().build();
  }
}
