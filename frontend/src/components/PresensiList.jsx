import React, { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import api from '../services/api'

function PresensiList({ userInfo, onLogout }) {
  const [presensiList, setPresensiList] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [tglAwal, setTglAwal] = useState('')
  const [tglAkhir, setTglAkhir] = useState('')

  const loadPresensi = async () => {
    if (!tglAwal || !tglAkhir) {
      setError('Pilih tanggal awal dan akhir')
      return
    }

    setLoading(true)
    setError('')

    try {
      const tglAwalEpoch = Math.floor(new Date(tglAwal).getTime() / 1000)
      const tglAkhirEpoch = Math.floor(new Date(tglAkhir).getTime() / 1000)

      let response
      if (userInfo?.profile === 'Admin') {
        response = await api.get('/presensi/daftar/admin', {
          params: { tglAwal: tglAwalEpoch, tglAkhir: tglAkhirEpoch }
        })
      } else {
        response = await api.get('/presensi/daftar/pegawai', {
          params: { tglAwal: tglAwalEpoch, tglAkhir: tglAkhirEpoch }
        })
      }

      setPresensiList(response.data)
    } catch (err) {
      setError(err.response?.data || 'Gagal memuat data presensi')
    } finally {
      setLoading(false)
    }
  }

  const formatEpoch = (epoch) => {
    if (!epoch) return '-'
    const date = new Date(epoch * 1000)
    return date.toLocaleDateString('id-ID')
  }

  // Set default dates (current month)
  useEffect(() => {
    const now = new Date()
    const firstDay = new Date(now.getFullYear(), now.getMonth(), 1)
    const lastDay = new Date(now.getFullYear(), now.getMonth() + 1, 0)
    setTglAwal(firstDay.toISOString().split('T')[0])
    setTglAkhir(lastDay.toISOString().split('T')[0])
  }, [])

  // Auto load on mount
  useEffect(() => {
    if (tglAwal && tglAkhir) {
      loadPresensi()
    }
  }, [])

  return (
    <div>
      <nav className="navbar">
        <h1>HRMIS - Daftar Presensi</h1>
        <div>
          <Link to="/dashboard" style={{ color: 'white', marginRight: '15px', textDecoration: 'none' }}>Dashboard</Link>
          <button onClick={onLogout}>Logout</button>
        </div>
      </nav>
      <div className="container">
        <div className="card">
          <h2>Daftar Presensi {userInfo?.profile === 'Admin' ? '(Semua Pegawai)' : '(Saya)'}</h2>
          
          <div style={{ display: 'flex', gap: '15px', marginBottom: '20px', alignItems: 'end' }}>
            <div className="form-group" style={{ flex: 1 }}>
              <label>Tanggal Awal</label>
              <input
                type="date"
                value={tglAwal}
                onChange={(e) => setTglAwal(e.target.value)}
              />
            </div>
            <div className="form-group" style={{ flex: 1 }}>
              <label>Tanggal Akhir</label>
              <input
                type="date"
                value={tglAkhir}
                onChange={(e) => setTglAkhir(e.target.value)}
              />
            </div>
            <button className="btn btn-primary" onClick={loadPresensi} disabled={loading}>
              {loading ? 'Loading...' : 'Cari'}
            </button>
          </div>

          {error && <div className="error">{error}</div>}

          {loading ? (
            <p>Loading...</p>
          ) : (
            <table className="table">
              <thead>
                <tr>
                  {userInfo?.profile === 'Admin' && <th>Nama Pegawai</th>}
                  <th>Tanggal</th>
                  <th>Jam Masuk</th>
                  <th>Jam Keluar</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {presensiList.length === 0 ? (
                  <tr>
                    <td colSpan={userInfo?.profile === 'Admin' ? 5 : 4} style={{ textAlign: 'center' }}>
                      Tidak ada data presensi
                    </td>
                  </tr>
                ) : (
                  presensiList.map((presensi, index) => (
                    <tr key={index}>
                      {userInfo?.profile === 'Admin' && <td>{presensi.namaLengkap}</td>}
                      <td>{formatEpoch(presensi.tglAbsensi)}</td>
                      <td>{presensi.jamMasuk || '-'}</td>
                      <td>{presensi.jamKeluar || '-'}</td>
                      <td>{presensi.namaStatus || '-'}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  )
}

export default PresensiList

