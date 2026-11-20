package br.edu.utfpr.td.tsi.exception;

public class PacienteNaoEncontradoException extends RuntimeException {

    public PacienteNaoEncontradoException() {
        super("Paciente não encontrado.");
    }

    public PacienteNaoEncontradoException(String id) {
        super("Paciente não encontrado com ID: " + id);
    }
}