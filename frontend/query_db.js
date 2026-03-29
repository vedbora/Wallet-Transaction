import mysql from 'mysql2/promise';
async function run() {
  const conn = await mysql.createConnection('mysql://root:hqSBRVRTTkVraUSkVEydcFCortMvwBSX@viaduct.proxy.rlwy.net:26163/Wallet');
  const [users] = await conn.execute('SELECT id, email, role FROM users');
  console.log('Users found:', users);
  
  await conn.execute("UPDATE users SET role = 'ADMIN' WHERE role != 'ADMIN'");
  console.log('Roles updated to ADMIN successfully.');
  
  await conn.end();
}
run().catch(console.error);
