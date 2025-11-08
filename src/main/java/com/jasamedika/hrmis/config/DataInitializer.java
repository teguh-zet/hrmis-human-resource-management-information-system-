package com.jasamedika.hrmis.config;

import com.jasamedika.hrmis.entity.*;
import com.jasamedika.hrmis.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private JabatanRepository jabatanRepository;

    @Autowired
    private DepartemenRepository departemenRepository;

    @Autowired
    private UnitKerjaRepository unitKerjaRepository;

    @Autowired
    private JenisKelaminRepository jenisKelaminRepository;

    @Autowired
    private PendidikanRepository pendidikanRepository;

    @Autowired
    private StatusAbsenRepository statusAbsenRepository;

    @Override
    public void run(String... args) throws Exception {
        // Initialize combo data if empty
        if (jabatanRepository.count() == 0) {
            initializeJabatan();
        }
        if (departemenRepository.count() == 0) {
            initializeDepartemen();
        }
        if (unitKerjaRepository.count() == 0) {
            initializeUnitKerja();
        }
        if (jenisKelaminRepository.count() == 0) {
            initializeJenisKelamin();
        }
        if (pendidikanRepository.count() == 0) {
            initializePendidikan();
        }
        if (statusAbsenRepository.count() == 0) {
            initializeStatusAbsen();
        }
    }

    private void initializeJabatan() {
        jabatanRepository.save(new Jabatan(null, "Manager"));
        jabatanRepository.save(new Jabatan(null, "Supervisor"));
        jabatanRepository.save(new Jabatan(null, "Staff"));
        jabatanRepository.save(new Jabatan(null, "Admin"));
    }

    private void initializeDepartemen() {
        departemenRepository.save(new Departemen(null, "HRD"));
        departemenRepository.save(new Departemen(null, "IT"));
        departemenRepository.save(new Departemen(null, "Finance"));
        departemenRepository.save(new Departemen(null, "Marketing"));
        departemenRepository.save(new Departemen(null, "Operations"));
    }

    private void initializeUnitKerja() {
        unitKerjaRepository.save(new UnitKerja(null, "Jakarta"));
        unitKerjaRepository.save(new UnitKerja(null, "Bandung"));
        unitKerjaRepository.save(new UnitKerja(null, "Surabaya"));
        unitKerjaRepository.save(new UnitKerja(null, "Yogyakarta"));
    }

    private void initializeJenisKelamin() {
        jenisKelaminRepository.save(new JenisKelamin(null, "Laki-laki"));
        jenisKelaminRepository.save(new JenisKelamin(null, "Perempuan"));
    }

    private void initializePendidikan() {
        pendidikanRepository.save(new Pendidikan(null, "SD"));
        pendidikanRepository.save(new Pendidikan(null, "SMP"));
        pendidikanRepository.save(new Pendidikan(null, "SMA"));
        pendidikanRepository.save(new Pendidikan(null, "D3"));
        pendidikanRepository.save(new Pendidikan(null, "S1"));
        pendidikanRepository.save(new Pendidikan(null, "S2"));
        pendidikanRepository.save(new Pendidikan(null, "S3"));
    }

    private void initializeStatusAbsen() {
        statusAbsenRepository.save(new StatusAbsen(null, "Masuk"));
        statusAbsenRepository.save(new StatusAbsen(null, "Izin"));
        statusAbsenRepository.save(new StatusAbsen(null, "Sakit"));
        statusAbsenRepository.save(new StatusAbsen(null, "Cuti"));
        statusAbsenRepository.save(new StatusAbsen(null, "Alpha"));
    }
}

