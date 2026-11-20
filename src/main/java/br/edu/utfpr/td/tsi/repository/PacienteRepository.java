package br.edu.utfpr.td.tsi.repository;

import br.edu.utfpr.td.tsi.model.Paciente;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PacienteRepository extends MongoRepository<Paciente, String> {
    Optional<Paciente> findByCpf(String cpf);
}