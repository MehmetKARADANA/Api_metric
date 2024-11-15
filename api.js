const express = require('express');
const sql = require('mssql');
const bodyParser = require('body-parser');

const app = express();
app.use(bodyParser.json());

// MSSQL veritabanı bağlantı yapılandırması
const dbConfig = {
    user: 'DESKTOP-A593K9I\Mehmet',
    password: '',
    server: 'localhost',
    database: 'users_db',
    options: {
      
        trustServerCertificate: true, // Yerel sunucu için true olmalı
        integratedSecurity: true  
    }
};
/*
const dbConfig = {
    server: 'localhost',
    database: 'users_db',
    options: {
        encrypt: true,
        trustServerCertificate: true // Yerel sunucu için true olmalı
    },
    authentication: {
        type: 'ntlm',
        options: {
            domain: 'DESKTOP-A593K9I', // Bilgisayarın adı
            userName: 'Mehmet',         // Windows kullanıcı adın
            password: ''                // Parola boş olabilir, güvenli değilse bırakılabilir
        }
    }
};
*/
// MSSQL bağlantısını sağlama
sql.connect(dbConfig).then(pool => {
    if (pool.connecting) {
        console.log("Veritabanına bağlanıyor...");
    } else if (pool.connected) {
        console.log("Veritabanına bağlandı!");
    }

    // Flutter frontend'in kullanıcı verilerini çekebileceği bir endpoint
    app.get('/users', async (req, res) => {
        try {
            const result = await pool.request().query('SELECT * FROM Users'); // Kullanıcı tablosunu sorgula
            res.status(200).json(result.recordset); // JSON formatında sonucu döndür
        } catch (err) {
            res.status(500).json({ message: 'Veritabanı hatası', error: err });
        }
    });

    // Yeni kullanıcı eklemek için POST endpointi
    app.post('/users', async (req, res) => {
        const { name, email } = req.body;
        try {
            await pool.request()
                .input('name', sql.VarChar, name)
                .input('email', sql.VarChar, email)
                .query('INSERT INTO Users (name, email) VALUES (@name, @email)');
            res.status(201).json({ message: 'Kullanıcı başarıyla eklendi!' });
        } catch (err) {
            res.status(500).json({ message: 'Kullanıcı eklenemedi', error: err });
        }
    });

}).catch(err => console.log("Veritabanı bağlantı hatası:", err));

// Sunucu başlatma
const PORT = 3000;
app.listen(PORT, () => {
    console.log(`Sunucu ${PORT} portunda çalışıyor`);
});
