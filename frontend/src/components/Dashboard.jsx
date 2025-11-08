import React from 'react'
import { Link } from 'react-router-dom'

function Dashboard({ userInfo, onLogout }) {
  const formatEpoch = (epoch) => {
    if (!epoch) return '-'
    const date = new Date(epoch * 1000)
    return date.toLocaleDateString('id-ID')
  }

  return (
    <div>
      <nav className="navbar">
        <h1>HRMIS - Dashboard</h1>
        <div>
          <span style={{ marginRight: '15px' }}>Welcome, {userInfo?.namaLengkap}</span>
          <button onClick={onLogout}>Logout</button>
        </div>
      </nav>
      <div className="container">
        <div className="card">
          <h2>Informasi User</h2>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px', marginTop: '20px' }}>
            <div>
              <strong>Nama Lengkap:</strong> {userInfo?.namaLengkap}
            </div>
            <div>
              <strong>Email:</strong> {userInfo?.email}
            </div>
            <div>
              <strong>Profile:</strong> {userInfo?.profile}
            </div>
            <div>
              <strong>Jabatan:</strong> {userInfo?.namaJabatan || '-'}
            </div>
            <div>
              <strong>Departemen:</strong> {userInfo?.namaDepartemen || '-'}
            </div>
            <div>
              <strong>Unit Kerja:</strong> {userInfo?.namaUnitKerja || '-'}
            </div>
            <div>
              <strong>Tanggal Lahir:</strong> {formatEpoch(userInfo?.tanggalLahir)}
            </div>
            <div>
              <strong>Jenis Kelamin:</strong> {userInfo?.namaJenisKelamin || '-'}
            </div>
          </div>
        </div>

        <div className="card">
          <h2>Menu</h2>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '15px', marginTop: '20px' }}>
            {(userInfo?.profile === 'Admin' || userInfo?.namaDepartemen === 'HRD') && (
              <Link to="/pegawai" style={{ textDecoration: 'none' }}>
                <div className="card" style={{ textAlign: 'center', cursor: 'pointer', transition: 'transform 0.2s' }}>
                  <h3>Manajemen Pegawai</h3>
                  <p>Kelola data pegawai</p>
                </div>
              </Link>
            )}
            <Link to="/presensi" style={{ textDecoration: 'none' }}>
              <div className="card" style={{ textAlign: 'center', cursor: 'pointer', transition: 'transform 0.2s' }}>
                <h3>Daftar Presensi</h3>
                <p>Lihat riwayat presensi</p>
              </div>
            </Link>
            <Link to="/presensi/check" style={{ textDecoration: 'none' }}>
              <div className="card" style={{ textAlign: 'center', cursor: 'pointer', transition: 'transform 0.2s' }}>
                <h3>Check In/Out</h3>
                <p>Absensi harian</p>
              </div>
            </Link>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Dashboard

