package br.edu.utfpr.td.tsi.service;

import br.edu.utfpr.td.tsi.exception.PacienteNaoEncontradoException;
import br.edu.utfpr.td.tsi.model.Paciente;
import br.edu.utfpr.td.tsi.repository.PacienteRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class PacienteService {

    private static final Logger log = LoggerFactory.getLogger(PacienteService.class);

    private final PacienteRepository repository;
    private final MongoTemplate mongoTemplate;

    public PacienteService(PacienteRepository repository, MongoTemplate mongoTemplate) {
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
    }

    public List<Paciente> cadastrar(List<Paciente> pacientes) {
        if (pacientes == null || pacientes.isEmpty()) {
            throw new IllegalArgumentException("Nenhum paciente informado.");
        }

        log.debug("Recebido pedido de cadastro para {} paciente(s)", pacientes.size());

        for (Paciente paciente : pacientes) {
            normalizar(paciente);
            validar(paciente);

            paciente.setId(null);

            Optional<Paciente> existente = repository.findByCpf(paciente.getCpf());
            if (existente.isPresent()) {
                log.warn("CPF duplicado: {}", paciente.getCpf());
                throw new IllegalArgumentException(
                        "CPF " + paciente.getCpf() + " já cadastrado.");
            }
        }

        List<Paciente> salvos = repository.saveAll(pacientes);
        log.info("Cadastrados {} paciente(s) com sucesso", salvos.size());
        return salvos;
    }

    public List<Paciente> pesquisar(String nome, String cpf, String dataNascimento) {
        Query query = new Query();
        boolean temFiltro = false;

        if (temValor(nome)) {
            query.addCriteria(Criteria.where("NOME_COMPLETO").regex(Pattern.quote(nome.trim()), "i"));
            temFiltro = true;
        }

        if (temValor(cpf)) {
            String cpfLimpo = cpf.replaceAll("\\D", "");
            query.addCriteria(Criteria.where("CPF").is(cpfLimpo));
            temFiltro = true;
        }

        if (temValor(dataNascimento)) {
            LocalDate data = LocalDate.parse(dataNascimento.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            query.addCriteria(Criteria.where("DATA_NASCIMENTO").is(data));
            temFiltro = true;
        }

        if (!temFiltro) {
            return List.of();
        }

        return mongoTemplate.find(query, Paciente.class);
    }


    public Paciente buscarPorId(String id) {
        log.debug("Buscando paciente por ID: {}", id);
        return repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Paciente não encontrado: {}", id);
                    return new PacienteNaoEncontradoException(id);
                });
    }

    public Paciente atualizar(String id, Paciente novosDados) {
        log.debug("Atualizando paciente ID: {}", id);

        Paciente paciente = buscarPorId(id);

        normalizar(novosDados);
        validar(novosDados);

        Optional<Paciente> existente = repository.findByCpf(novosDados.getCpf());

        if (existente.isPresent() && !existente.get().getId().equals(id)) {
            log.warn("CPF duplicado na atualização: {}", novosDados.getCpf());
            throw new IllegalArgumentException(
                    "CPF " + novosDados.getCpf() + " já cadastrado.");
        }

        paciente.setNomeCompleto(novosDados.getNomeCompleto());
        paciente.setCpf(novosDados.getCpf());
        paciente.setDataNascimento(novosDados.getDataNascimento());
        paciente.setSexo(novosDados.getSexo());
        paciente.setTelefone(novosDados.getTelefone());
        paciente.setEmail(novosDados.getEmail());
        paciente.setEndereco(novosDados.getEndereco());
        paciente.setTipoSanguineo(novosDados.getTipoSanguineo());
        paciente.setAlergias(novosDados.getAlergias());
        paciente.setContatoEmergencia(novosDados.getContatoEmergencia());
        if (novosDados.getAtivo() != null) {
            paciente.setAtivo(novosDados.getAtivo());
        }

        Paciente salvo = repository.save(paciente);
        log.info("Paciente atualizado: {} ({})", salvo.getNomeCompleto(), id);
        return salvo;
    }

    public void deletar(String id) {
        if (!temValor(id)) {
            throw new IllegalArgumentException("ID do paciente não informado.");
        }
        log.debug("Deletando paciente ID: {}", id);
        Optional<Paciente> paciente = repository.findById(id);
        if (!paciente.isPresent()) {
            log.warn("Paciente não encontrado para deleção: {}", id);
            throw new PacienteNaoEncontradoException(id);
        }
        repository.delete(paciente.get());
        log.info("Paciente deletado: {} ({})", paciente.get().getNomeCompleto(), id);
    }

    public long contar() {
        return repository.count();
    }

    private void validar(Paciente paciente) {
        if (paciente == null) throw new IllegalArgumentException("Paciente não informado.");
        if (!temValor(paciente.getNomeCompleto())) throw new IllegalArgumentException("Nome é obrigatório.");
        if (!temValor(paciente.getCpf())) throw new IllegalArgumentException("CPF é obrigatório.");
        if (!paciente.getCpf().matches("\\d{11}")) {
            throw new IllegalArgumentException("CPF deve conter exatamente 11 dígitos numéricos.");
        }
    }

    private void normalizar(Paciente paciente) {
        if (paciente == null) return;

        paciente.setNomeCompleto(upper(paciente.getNomeCompleto()));
        paciente.setSexo(upper(paciente.getSexo()));
        paciente.setTelefone(upper(paciente.getTelefone()));
        paciente.setEndereco(upper(paciente.getEndereco()));
        paciente.setTipoSanguineo(upper(paciente.getTipoSanguineo()));
        paciente.setContatoEmergencia(upper(paciente.getContatoEmergencia()));
        paciente.setEmail(lower(paciente.getEmail()));

        if (temValor(paciente.getCpf())) {
            paciente.setCpf(paciente.getCpf().replaceAll("\\D", ""));
        }

        paciente.setAlergias(upperList(paciente.getAlergias()));
    }

    private String upper(String valor) {
        return valor == null ? null : valor.trim().toUpperCase();
    }

    private String lower(String valor) {
        return valor == null ? null : valor.trim().toLowerCase();
    }

    private List<String> upperList(List<String> valores) {
        if (valores == null) return null;
        List<String> resultado = new ArrayList<>();
        for (String valor : valores) {
            if (valor != null && !valor.isBlank()) {
                resultado.add(valor.trim().toUpperCase());
            }
        }
        return resultado;
    }

    private boolean temValor(String valor) {
        return valor != null && !valor.isBlank();
    }
}
