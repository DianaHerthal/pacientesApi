package br.edu.utfpr.td.tsi.controller;

import br.edu.utfpr.td.tsi.model.Paciente;
import br.edu.utfpr.td.tsi.service.PacienteService;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pacientes")
@CrossOrigin(origins = "*")
public class PacienteController {

    private static final Logger log = LoggerFactory.getLogger(PacienteController.class);

    private final PacienteService service;

    public PacienteController(PacienteService service) {
        this.service = service;
    }

    @PostMapping("/cadastrar")
    public List<Paciente> cadastrar(@Valid @RequestBody List<Paciente> pacientes) {
        log.info("POST /pacientes/cadastrar — {} paciente(s)", pacientes.size());
        return service.cadastrar(pacientes);
    }

    @GetMapping
    public List<Paciente> pesquisar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String crm,
            @RequestParam(required = false) String especialidade) {

        log.debug("GET /pacientes?nome={}&crm={}&especialidade={}",
                nome, crm, especialidade);

        if (vazio(nome) && vazio(crm) && vazio(especialidade)) {
            return List.of();
        }

        return service.pesquisar(nome, crm, especialidade);
    }

    @GetMapping("/{id}")
    public Paciente buscar(@PathVariable String id) {
        log.debug("GET /pacientes/{}", id);
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public Paciente atualizar(
            @PathVariable String id,
            @RequestBody Paciente paciente) {
        log.info("PUT /pacientes/{}", id);
        return service.atualizar(id, paciente);
    }

    @DeleteMapping("/{id}")
    public String deletar(@PathVariable String id) {
        log.info("DELETE /pacientes/{}", id);
        service.deletar(id);
        return "Paciente deletado com sucesso!";
    }

    private boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }
}