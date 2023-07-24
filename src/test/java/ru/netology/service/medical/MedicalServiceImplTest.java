package ru.netology.service.medical;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import ru.netology.patient.entity.BloodPressure;
import ru.netology.patient.entity.HealthInfo;
import ru.netology.patient.entity.PatientInfo;
import ru.netology.patient.repository.PatientInfoRepository;
import ru.netology.patient.service.alert.SendAlertService;
import ru.netology.patient.service.medical.MedicalServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;

public class MedicalServiceImplTest {
    public PatientInfoRepository patientInfoRepository;
    public SendAlertService alertService;
    public MedicalServiceImpl medicalService;

    public static Stream<Arguments> checkBloodPressureArguments() {
        return Stream.of(
            Arguments.of(new PatientInfo("Иван", "Петров", LocalDate.of(1980, 11, 26),
                new HealthInfo(new BigDecimal("36.65"), new BloodPressure(120, 80))), new BloodPressure(60, 120)),
            Arguments.of(new PatientInfo("Семен", "Михайлов", LocalDate.of(1982, 1, 16),
                new HealthInfo(new BigDecimal("36.6"), new BloodPressure(125, 78))), new BloodPressure(60, 120))
        );
    }

    public static Stream<Arguments> checkTemperatureArguments() {
        return Stream.of(
            Arguments.of(new PatientInfo("Иван", "Петров", LocalDate.of(1980, 11, 26),
                new HealthInfo(new BigDecimal("36.65"), new BloodPressure(120, 80))), new BigDecimal("35")),
            Arguments.of(new PatientInfo("Семен", "Михайлов", LocalDate.of(1982, 1, 16),
                new HealthInfo(new BigDecimal("36.6"), new BloodPressure(125, 78))), new BigDecimal("35"))
        );
    }

    @BeforeEach
    public void initTest() {
        this.patientInfoRepository = Mockito.mock(PatientInfoRepository.class);
        this.alertService = Mockito.mock(SendAlertService.class);
        this.medicalService = new MedicalServiceImpl(this.patientInfoRepository, this.alertService);
    }

    @AfterEach
    public void finalizeTest() {
        this.patientInfoRepository = null;
        this.alertService = null;
        this.medicalService = null;
    }

    @Test
    public void testCheckBloodPressureWithoutMessage() {
        // given
        final PatientInfo patientInfo = new PatientInfo("Иван", "Петров", LocalDate.of(1980, 11, 26),
            new HealthInfo(new BigDecimal("36.65"), new BloodPressure(120, 80)));
        final BloodPressure bloodPressure = new BloodPressure(120, 80);

        // when
        Mockito.when(this.patientInfoRepository.getById(Mockito.anyString())).thenReturn(patientInfo);
        this.medicalService.checkBloodPressure(Mockito.anyString(), bloodPressure);

        //assert
        Mockito.verify(this.alertService, Mockito.times(0)).send(Mockito.anyString());
    }

    @Test
    public void testCheckTemperatureWithoutMessage() {
        // given
        final PatientInfo patientInfo = new PatientInfo("Иван", "Петров", LocalDate.of(1980, 11, 26),
            new HealthInfo(new BigDecimal("36.65"), new BloodPressure(120, 80)));
        final BigDecimal temperature = new BigDecimal("38");

        // when
        Mockito.when(this.patientInfoRepository.getById(Mockito.anyString())).thenReturn(patientInfo);
        this.medicalService.checkTemperature(Mockito.anyString(), temperature);

        //assert
        Mockito.verify(this.alertService, Mockito.times(0)).send(Mockito.anyString());
    }

    @ParameterizedTest
    @MethodSource("checkBloodPressureArguments")
    public void testCheckBloodPressure(PatientInfo patientInfo, BloodPressure bloodPressure) {
        // given
        final ArgumentCaptor<String> bloodPressureArgumentCaptor = ArgumentCaptor.forClass(String.class);

        // when
        Mockito.when(this.patientInfoRepository.getById(Mockito.anyString())).thenReturn(patientInfo);
        this.medicalService.checkBloodPressure(Mockito.anyString(), bloodPressure);

        //assert
        Mockito.verify(this.alertService, Mockito.times(1)).send(bloodPressureArgumentCaptor.capture());
        assertThat(bloodPressureArgumentCaptor.getValue(), startsWith("Warning, patient with id"));
    }

    @ParameterizedTest
    @MethodSource("checkTemperatureArguments")
    public void testCheckTemperature(PatientInfo patientInfo, BigDecimal temperature) {
        // given
        final ArgumentCaptor<String> temperatureArgumentCaptor = ArgumentCaptor.forClass(String.class);

        // when
        Mockito.when(this.patientInfoRepository.getById(Mockito.anyString())).thenReturn(patientInfo);
        this.medicalService.checkTemperature(Mockito.anyString(), temperature);

        //assert
        Mockito.verify(this.alertService, Mockito.times(1)).send(temperatureArgumentCaptor.capture());
        assertThat(temperatureArgumentCaptor.getValue(), startsWith("Warning, patient with id"));
    }
}
