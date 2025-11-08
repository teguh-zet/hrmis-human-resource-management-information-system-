import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../services/api'

function Dashboard({ userInfo: initialUserInfo, onLogout, setUserInfo: setUserInfoParent }) {
  const [userInfo, setUserInfo] = useState(initialUserInfo)
  const [loading, setLoading] = useState(false)
  const [showPhotoForm, setShowPhotoForm] = useState(false)
  const [photoFile, setPhotoFile] = useState(null)
  const [photoFileName, setPhotoFileName] = useState('')
  const [photoError, setPhotoError] = useState('')
  const [photoLoading, setPhotoLoading] = useState(false)

  useEffect(() => {
    // Refresh user info dari server saat component dimount
    const fetchUserInfo = async () => {
      try {
        setLoading(true)
        const response = await api.get('/auth/me')
        if (response.data) {
          setUserInfo(response.data)
          // Update localStorage dan parent state
          localStorage.setItem('userInfo', JSON.stringify(response.data))
          if (setUserInfoParent) {
            setUserInfoParent(response.data)
          }
        }
      } catch (error) {
        console.error('Error fetching user info:', error)
        // Jika error, tetap gunakan initialUserInfo
      } finally {
        setLoading(false)
      }
    }

    fetchUserInfo()
  }, [setUserInfoParent])

  const formatEpoch = (epoch) => {
    if (!epoch) return '-'
    const date = new Date(epoch * 1000)
    return date.toLocaleDateString('id-ID')
  }

  const handlePhotoChange = (e) => {
    const file = e.target.files[0]
    if (file) {
      setPhotoFile(file)
      setPhotoFileName(file.name)
      setPhotoError('')
    }
  }

  const handlePhotoSubmit = async (e) => {
    e.preventDefault()
    if (!photoFile || !photoFileName) {
      setPhotoError('Pilih file foto dan isi nama file')
      return
    }

    try {
      setPhotoLoading(true)
      setPhotoError('')
      const formData = new FormData()
      formData.append('namaFile', photoFileName)
      formData.append('files', photoFile)

      await api.post('/pegawai/ubah-photo', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })

      // Refresh user info setelah ubah foto
      const response = await api.get('/auth/me')
      if (response.data) {
        setUserInfo(response.data)
        localStorage.setItem('userInfo', JSON.stringify(response.data))
        if (setUserInfoParent) {
          setUserInfoParent(response.data)
        }
      }

      setShowPhotoForm(false)
      setPhotoFile(null)
      setPhotoFileName('')
      alert('Foto berhasil diubah')
    } catch (err) {
      setPhotoError(err.response?.data || 'Gagal mengubah foto')
    } finally {
      setPhotoLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="container">
        <div className="card">
          <p>Memuat data...</p>
        </div>
      </div>
    )
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
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <h2>Informasi User</h2>
            <button className="btn btn-secondary" onClick={() => setShowPhotoForm(true)}>
              Ubah Foto
            </button>
          </div>
          {userInfo?.photo && (
            <div style={{ textAlign: 'center', marginTop: '20px' }}>
              <img 
                src={`http://localhost:8080/api/files/${userInfo.photo}`} 
                alt="Foto Profil" 
                style={{ maxWidth: '200px', maxHeight: '200px', borderRadius: '8px' }}
                onError={(e) => {
                  e.target.style.display = 'none'
                }}
              />
            </div>
          )}
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

        {showPhotoForm && (
          <div className="card" style={{ marginTop: '20px', background: '#f8f9fa' }}>
            <h3>Ubah Foto Profil</h3>
            <form onSubmit={handlePhotoSubmit}>
              <div className="form-group">
                <label>Nama File *</label>
                <input 
                  type="text" 
                  value={photoFileName} 
                  onChange={(e) => setPhotoFileName(e.target.value)} 
                  placeholder="contoh: foto-profil.jpg"
                  required 
                />
              </div>
              <div className="form-group">
                <label>Pilih File Foto *</label>
                <input 
                  type="file" 
                  accept="image/*" 
                  onChange={handlePhotoChange}
                  required 
                />
              </div>
              {photoError && <div className="error">{photoError}</div>}
              <div style={{ marginTop: '20px' }}>
                <button type="submit" className="btn btn-primary" disabled={photoLoading}>
                  {photoLoading ? 'Mengunggah...' : 'Unggah Foto'}
                </button>
                <button 
                  type="button" 
                  className="btn btn-secondary" 
                  style={{ marginLeft: '10px' }} 
                  onClick={() => {
                    setShowPhotoForm(false)
                    setPhotoFile(null)
                    setPhotoFileName('')
                    setPhotoError('')
                  }}>
                  Batal
                </button>
              </div>
            </form>
          </div>
        )}

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

