package project.quotidien.relance.creationcompte;

import project.annotation.MyCustomAnnotation;
import project.dao.UtilisateurRepository;
import project.dao.UtilisateurSuiviRepository;
import project.entite.enumeration.ParametreEnum;
import project.metier.service.ParametreBusinessService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@MyCustomBatchUnitTest 
@TestPropertySource(properties = {"spring.batch.job.names=jobRelanceCreationCompte"})
public class JobTest {
    @Autowired
    private JobLauncherTestUtils jobLauncherTest;

    @MockBean
    private UtilisateurSuiviRepository utilisateurSuiviRepository;

    @MockBean
    private UtilisateurRepository utilisateurRepository;

    @MockBean
    private ParametreBusinessService parametreBusinessService;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void cas1() throws Exception {
        when(utilisateurSuiviRepository.findUtilisateursInactifsPourPremiereRelance(anyInt()))
                .thenReturn(Collections.singletonList(new Object[]{1L, Instant.now()}));

        when(parametreBusinessService
                .rechercherIntegerValueParametre(ParametreEnum.MAX_RELANCE_CREATION_COMPTE.name()))
                .thenReturn(8);

        when(parametreBusinessService
                .rechercherIntegerValueParametre(ParametreEnum.DELAI_PREMIERE_RELANCE_EN_JOURS_1.name()))
                .thenReturn(3);

        when(utilisateurRepository
                .findUtilisateursByListOfIdsAndEmailNotNull(Collections.singletonList(1L)))
                .thenReturn(Collections.singletonList(new Object[]{
                        "user.test@cs4.test-cdc.r", Instant.now(), "user", "test", "fr", 1L
                }));


        JobExecution jobExecution = jobLauncherTest.launchStep("stepPremiereRelance");

        assertEquals(BatchStatus.COMPLETED.toString(), jobExecution.getExitStatus().getExitCode());
    }


    @Test
    public void cas2() throws Exception {
      // the issue is here ↓ ↓ : I'm getting also the result of the cas1() method
        when(utilisateurSuiviRepository.findUtilisateursInactifsPourPremiereRelance(anyInt()))
                .thenReturn(Collections.singletonList(new Object[]{2L, "2023-01-21 15:32:32"}));
        

        when(parametreBusinessService
                .rechercherIntegerValueParametre(ParametreEnum.MAX_RELANCE_CREATION_COMPTE.name()))
                .thenReturn(9);

        when(parametreBusinessService
                .rechercherIntegerValueParametre(ParametreEnum.DELAI_PREMIERE_RELANCE_EN_JOURS_1.name()))
                .thenReturn(3);

        when(parametreBusinessService
                .rechercherIntegerValueParametre(ParametreEnum.DELAI_PREMIERE_RELANCE_EN_JOURS_2.name()))
                .thenReturn(10);

        when(utilisateurRepository
                .findUtilisateursByListOfIdsAndEmailNotNull(Collections.singletonList(2L)))
                .thenReturn(Collections.singletonList(
                        new Object[]{
                                "user.test@cs4.test-cdc.r",
                                Instant.now().minus(4, ChronoUnit.DAYS), "user", "test", "fr", 2L

                        }));

        JobExecution jobExecution = jobLauncherTest.launchStep("stepPremiereRelance");
        assertEquals(BatchStatus.COMPLETED.toString(), jobExecution.getExitStatus().getExitCode());
    }


}
