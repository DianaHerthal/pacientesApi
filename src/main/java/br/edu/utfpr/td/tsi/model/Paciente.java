package br.edu.utfpr.td.tsi.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.util.List;

@Document(collection = "pacientes")
@Getter
@Setter
public class Paciente {

    @Id
    private String id;

    @Field("NOME_COMPLETO")
    @NotBlank(message = "Nome completo é obrigatório.")
    @Size(min = 3, max = 150, message = "O nome deve ter entre 3 e 150 caracteres.")
    private String nomeCompleto;

    @Field("CPF")
    @Indexed(unique = true)
    @NotBlank(message = "CPF é obrigatório.")
    @Pattern(regexp = "\\d{11}", message = "CPF deve conter exatamente 11 dígitos numéricos.")
    private String cpf;

    @Field("DATA_NASCIMENTO")
    @NotNull(message = "Data de nascimento é obrigatória.")
    @Past(message = "A data de nascimento deve estar no passado.")
    private LocalDate dataNascimento;

    @Field("SEXO")
    @NotBlank(message = "Sexo biológico é obrigatório.")
    private String sexo; 

    @Field("TELEFONE")
    @NotBlank(message = "Telefone é obrigatório.")
    private String telefone;

    @Field("EMAIL")
    @Email(message = "E-mail inválido.")
    private String email;

    @Field("ENDERECO")
    private String endereco; 

    @Field("TIPO_SANGUINEO")
    private String tipoSanguineo; 

    @Field("ALERGIAS")
    private List<String> alergias; 

    @Field("CONTATO_EMERGENCIA")
    private String contatoEmergencia;

    @Field("ATIVO")
    private Boolean ativo = true;

}
