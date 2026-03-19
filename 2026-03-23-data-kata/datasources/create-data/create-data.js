// Fake data generator for DB and CSV (no Kafka)
import { Client as PgClient } from 'pg';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import { createObjectCsvWriter as createCsvWriter } from 'csv-writer';

// --- Data generation logic ---
import { generateStores, generateSalesmen, generateSale } from './data-generation.js';

// --- Config ---
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const config = {
  storeCount: 81, // initial number of stores for the pool
  salesmanCount: 80, // initial number of salesmen for the pool
  dbSales: {
    connectionString: process.env.SALES_DB_URL,
    table: process.env.SALES_DB_TABLE || 'sales'
  },
  dbSalesmans: {
    connectionString: process.env.SALESMANS_DB_URL,
    table: process.env.SALESMANS_DB_TABLE || 'salesmans'
  },
  storesCsv: '/data/stores.csv'
};

// --- Main logic ---
async function main() {
  // Generate and insert data continuously, with delay
  // Generate stores and salesmen only once
  const stores = generateStores(config.storeCount);
  const salesmen = generateSalesmen(config.salesmanCount, stores);

  // Ensure directory exists
  const dataDir = path.dirname(config.storesCsv);
  if (!fs.existsSync(dataDir)) {
    fs.mkdirSync(dataDir, { recursive: true });
  }

  // Write to stores.csv continuously, one line at a time
  // If the file does not exist, write the header
  let storeId = 1;
  let writeHeader = !fs.existsSync(config.storesCsv);
  const csvWriter = createCsvWriter({
    path: config.storesCsv,
    header: [
      { id: 'id', title: 'id' },
      { id: 'name', title: 'name' },
      { id: 'city', title: 'city' },
      { id: 'state', title: 'state' },
      { id: 'country', title: 'country' }
    ],
    append: !writeHeader
  });
  if (writeHeader) {
    // Write header (csv-writer does this automatically on first write)
    console.log('Absolute path for stores.csv:', config.storesCsv);
  }

  // Open DB connections
  const salesDb = new PgClient({ connectionString: config.dbSales.connectionString });
  await salesDb.connect();
  const salesmansDb = new PgClient({ connectionString: config.dbSalesmans.connectionString });
  await salesmansDb.connect();

  let saleId = 1;
  let salesmanId = 1;
  const delayMs = Number(process.env.WRITE_DELAY_MS || 500); // default delay 500ms

  while (true) {
    // Generate and insert a sale
    const sale = generateSale(saleId++, salesmen);
    await salesDb.query(
      `INSERT INTO ${config.dbSales.table} (id, salesman_id, store_id, amount, sale_date, product_id, quantity) VALUES ($1,$2,$3,$4,$5,$6,$7)`,
      [sale.id, sale.salesman_id, sale.store_id, sale.amount, sale.sale_date, sale.product_id, sale.quantity]
    );
    console.log(`Inserted sale id=${sale.id}`);

    // Generate and insert a salesman (circular loop)
    const sm = salesmen[(salesmanId - 1) % salesmen.length];
    await salesmansDb.query(
      `INSERT INTO ${config.dbSalesmans.table} (id, name, store_id) VALUES ($1,$2,$3) ON CONFLICT (id) DO NOTHING`,
      [sm.id, sm.name, sm.store_id]
    );
    if (salesmanId <= salesmen.length) {
      console.log(`Inserted salesman id=${sm.id}`);
    }
    salesmanId++;

    // Generate and append a new store (circular loop)
    const store = stores[(storeId - 1) % stores.length];
    await csvWriter.writeRecords([store]);
    console.log(`Appended store id=${store.id} to CSV`);
    storeId++;

    // Delay
    await new Promise((resolve) => setTimeout(resolve, delayMs));
  }

  // Never gets here, but if it does, close connections
  // await salesDb.end();
  // await salesmansDb.end();
}

main().catch((err) => {
  console.error('Error:', err);
  process.exit(1);
});
