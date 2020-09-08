package com.pruebas.fperiago.SpringBatchHibernate.config;

import com.pruebas.fperiago.SpringBatchHibernate.entity.CanalComunicacio;
import com.pruebas.fperiago.SpringBatchHibernate.repository.CanalComunicacioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.persistence.EntityManagerFactory;

/**
 * SpringBatchHibernate - CASS
 * com.pruebas.fperiago.SpringBatchHibernate.config
 * 08/09/2020 - 8:57
 * f.periago.oliver
 */
// Clase de configuración que debe ser recogida por Spring Boot para conectar los beans y las dependencias.
@Configuration
// Habilitación del soporte por lotes (batch) para nuestra aplicación.
@EnableBatchProcessing
@Slf4j
public class BatchConfiguration {
    @Autowired
    public JobBuilderFactory jobBuilderFactory;
    @Autowired
    public StepBuilderFactory stepBuilderFactory;
    @Autowired
    EntityManagerFactory emf;
    @Autowired
    CanalComunicacioRepository canalComunicacioRepository;

    // Configuración del reader. Usamos FlatFileItemReader para leer de nuestro archivo CSV.
    // La ventaja de usar un lector incorporado es que maneja los fallos de la aplicación con elegancia y admite reinicios.
    // También puede omitir líneas durante errores con un límite de omisión configurable.
    @Bean
    public FlatFileItemReader reader() {
        FlatFileItemReader reader = new FlatFileItemReader<>();

        // La aplicación lee de un recurso de ruta de clase como se especifica en la línea 49.
        // Omitimos la línea de encabezado al especificar setLinesToSkip.
        reader.setResource(new ClassPathResource("canals-comunicacio-data.csv"));
        reader.setLinesToSkip(1);

        // Line Mapper se utiliza para asignar una línea leída del archivo a una representación utilizable por nuestra aplicación.
        // Utiliza a LineTokenizer para dividir una sola línea en tokens según los criterios especificados y a
        // FieldSetMapper para mapear los tokens en un conjunto de campos utilizables por nuestra aplicación.
        DefaultLineMapper lineMapper = new DefaultLineMapper<>();

        // Usamos DelimitedLineTokenizer para tokenizar las líneas dividiéndolas con una coma. De forma predeterminada,
        // la coma se utiliza como tokenizador. También especificamos los nombres de los tokens para que coincidan
        // con los campos de nuestra clase de modelo.
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("idCanal","descripcioCanal");

        // Aquí estamos usando BeanWrapperFieldSetMapper para mapear los datos a un bean por sus nombres de propiedad.
        // Los nombres de campo exactos se especifican en el tokenizador.
        BeanWrapperFieldSetMapper fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(CanalComunicacio.class);

        lineMapper.setFieldSetMapper(fieldSetMapper);
        lineMapper.setLineTokenizer(tokenizer);
        reader.setLineMapper(lineMapper);

        return reader;
    }

    // Configuración del writer. Aquí, estamos usando JpaItemWrite rpara escribir el objeto modelo en la base de datos.
    // JPA usa hibernate como proveedor de persistencia para conservar los datos.
    // El escritor solo necesita que el modelo se escriba en la base de datos.
    // Agrega los elementos recibidos del procesador y vacía los datos.
    @Bean
    public JpaItemWriter writer() {
        JpaItemWriter writer = new JpaItemWriter();
        writer.setEntityManagerFactory(emf);
        return writer;
    }

    // Configuración de la implementación personalizada del processor. Utilizamos una función lambda para
    // transformar el objeto CanalComunicacio entrante concantenando el identificador con el nombre del
    // canal de la comunicación.
    // EL PROCESADOR REALIZA SU EJECUCIÓN ELEMENTO A ELEMENTO (SÓLO UN ELEMENTO A LA VEZ).
    @Bean
    public ItemProcessor<CanalComunicacio, CanalComunicacio> processor() {
        return (item) -> {
          item.concatenateName();
          return item;
        };
    }

    // Configuración del Job.
    @Bean
    public Job importCanalJob(JobExecutionListener listener) {
        return jobBuilderFactory.get("importCanalJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(step1())
                .end()
                .build();
    }

    // Configuración del Step.
    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .<CanalComunicacio, CanalComunicacio>chunk(10)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }

    // JobExecutionListener ofrece los métodos beforeJob para ejecutar antes de que comience el trabajo y afterJob,
    // que se ejecuta después de que se haya completado el trabajo. Generalmente, estos métodos se utilizan para
    // recopilar varias métricas de trabajo y, a veces, inicializar constantes. Aquí, usamos afterJob para verificar si
    // los datos persistieron. Usamos un método de repositorio findAll para buscar todos los canales de nuestra
    // base de datos y mostrarlos.
    @Bean
    public JobExecutionListener listener() {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                // Por el momento lo dejamos vacío, pero podríamos añadir funcionalidad para que
                // realiza acciones antes de ejecutarse el job.
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
                    log.info("TRABAJO TERMINADO - Verifica los resultados");
                    canalComunicacioRepository.findAll()
                            .forEach(canal -> log.info("Encontrado ".concat(canal.getNombreCanal())
                                    .concat(" en la base de datos.")));
                }
            }
        };
    }
}
