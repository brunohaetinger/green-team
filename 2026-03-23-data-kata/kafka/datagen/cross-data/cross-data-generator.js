const { Kafka } = require('kafkajs');

const config = {
  brokers: (process.env.KAFKA_BROKERS || 'localhost:9092')
    .split(',')
    .map((b) => b.trim())
    .filter(Boolean),
  clientId: process.env.KAFKA_CLIENT_ID || 'cross-data-generator',
  topicSalesFs: process.env.TOPIC_SALES_FS || 'stores',
  topicSalesApi: process.env.TOPIC_SALES_API || 'salesmans',
  topicSalesDb: process.env.TOPIC_SALES_DB || 'sales',
  storeCount: Number(process.env.STORE_COUNT || 81),
  salesmanCount: Number(process.env.SALESMAN_COUNT || 80),
  salesPerSecond: Number(process.env.SALES_PER_SECOND || 5),
  salesFsPerSecond: Number(process.env.SALES_FS_PER_SECOND || 1),
  salesApiPerSecond: Number(process.env.SALES_API_PER_SECOND || 1)
};

const brazilLocations = [
  { state: 'AC', country: 'BR', cities: ['Rio Branco', 'Cruzeiro do Sul', 'Sena Madureira'] },
  { state: 'AL', country: 'BR', cities: ['Maceio', 'Arapiraca', 'Palmeira dos Indios'] },
  { state: 'AP', country: 'BR', cities: ['Macapa', 'Santana', 'Laranjal do Jari'] },
  { state: 'AM', country: 'BR', cities: ['Manaus', 'Parintins', 'Itacoatiara'] },
  { state: 'BA', country: 'BR', cities: ['Salvador', 'Feira de Santana', 'Vitoria da Conquista'] },
  { state: 'CE', country: 'BR', cities: ['Fortaleza', 'Caucaia', 'Juazeiro do Norte'] },
  { state: 'DF', country: 'BR', cities: ['Brasilia', 'Ceilandia', 'Taguatinga'] },
  { state: 'ES', country: 'BR', cities: ['Vitoria', 'Vila Velha', 'Serra'] },
  { state: 'GO', country: 'BR', cities: ['Goiania', 'Aparecida de Goiania', 'Anapolis'] },
  { state: 'MA', country: 'BR', cities: ['Sao Luis', 'Imperatriz', 'Caxias'] },
  { state: 'MT', country: 'BR', cities: ['Cuiaba', 'Varzea Grande', 'Rondonopolis'] },
  { state: 'MS', country: 'BR', cities: ['Campo Grande', 'Dourados', 'Tres Lagoas'] },
  { state: 'MG', country: 'BR', cities: ['Belo Horizonte', 'Uberlandia', 'Contagem'] },
  { state: 'PA', country: 'BR', cities: ['Belem', 'Ananindeua', 'Santarem'] },
  { state: 'PB', country: 'BR', cities: ['Joao Pessoa', 'Campina Grande', 'Santa Rita'] },
  { state: 'PR', country: 'BR', cities: ['Curitiba', 'Londrina', 'Maringa'] },
  { state: 'PE', country: 'BR', cities: ['Recife', 'Jaboatao dos Guararapes', 'Olinda'] },
  { state: 'PI', country: 'BR', cities: ['Teresina', 'Parnaiba', 'Picos'] },
  { state: 'RJ', country: 'BR', cities: ['Rio de Janeiro', 'Sao Goncalo', 'Duque de Caxias'] },
  { state: 'RN', country: 'BR', cities: ['Natal', 'Mossoro', 'Parnamirim'] },
  { state: 'RS', country: 'BR', cities: ['Porto Alegre', 'Caxias do Sul', 'Pelotas'] },
  { state: 'RO', country: 'BR', cities: ['Porto Velho', 'Ji-Parana', 'Ariquemes'] },
  { state: 'RR', country: 'BR', cities: ['Boa Vista', 'Rorainopolis', 'Caracarai'] },
  { state: 'SC', country: 'BR', cities: ['Florianopolis', 'Joinville', 'Blumenau'] },
  { state: 'SP', country: 'BR', cities: ['Sao Paulo', 'Guarulhos', 'Campinas'] },
  { state: 'SE', country: 'BR', cities: ['Aracaju', 'Nossa Senhora do Socorro', 'Lagarto'] },
  { state: 'TO', country: 'BR', cities: ['Palmas', 'Araguaina', 'Gurupi'] }
];

const cityStateCountry = brazilLocations.flatMap((entry) =>
  entry.cities.map((city) => ({ city, state: entry.state, country: entry.country }))
);

const firstNames = [
  'Adriana', 'Alan', 'Alessandra', 'Aline', 'Amanda', 'Ana', 'Andre', 'Angela',
  'Arthur', 'Barbara', 'Beatriz', 'Bernardo', 'Bianca', 'Bruna', 'Bruno', 'Caio',
  'Camila', 'Carla', 'Carlos', 'Carolina', 'Cecilia', 'Clara', 'Daniel', 'Danilo',
  'Debora', 'Diego', 'Eduarda', 'Eduardo', 'Elaine', 'Elisa', 'Emanuel', 'Fabiana',
  'Felipe', 'Fernanda', 'Flavia', 'Francisco', 'Gabriel', 'Gabriela', 'Gustavo', 'Helena',
  'Henrique', 'Igor', 'Isabela', 'Isadora', 'Ivan', 'Joao', 'Jorge', 'Jose',
  'Julia', 'Juliana', 'Kaique', 'Karen', 'Karina', 'Larissa', 'Laura', 'Leonardo',
  'Leticia', 'Lucas', 'Luana', 'Lucia', 'Luiza', 'Marcelo', 'Marcos', 'Maria',
  'Mariana', 'Mateus', 'Matheus', 'Melissa', 'Murilo', 'Natalia', 'Nicolas', 'Otavio',
  'Paula', 'Pedro', 'Priscila', 'Rafael', 'Raquel', 'Renata', 'Ricardo', 'Roberto',
  'Rodrigo', 'Sabrina', 'Samuel', 'Sofia', 'Tais', 'Tatiane', 'Thiago', 'Tiago',
  'Vanessa', 'Vitor', 'Vitoria', 'Yasmin'
];

const lastNames = [
  'Abreu', 'Albuquerque', 'Almeida', 'Alves', 'Amaral', 'Araujo', 'Assis', 'Barbosa',
  'Barros', 'Batista', 'Borges', 'Braga', 'Campos', 'Cardoso', 'Carneiro', 'Carvalho',
  'Castro', 'Cavalcante', 'Coelho', 'Costa', 'Cruz', 'Dias', 'Duarte', 'Esteves',
  'Farias', 'Fernandes', 'Ferreira', 'Figueiredo', 'Freitas', 'Garcia', 'Gomes', 'Goncalves',
  'Lacerda', 'Leite', 'Lima', 'Lopes', 'Machado', 'Macedo', 'Magalhaes', 'Marques',
  'Martins', 'Medeiros', 'Melo', 'Mendes', 'Miranda', 'Monteiro', 'Moraes', 'Moreira',
  'Nascimento', 'Neves', 'Nogueira', 'Novaes', 'Oliveira', 'Pacheco', 'Pereira', 'Pinto',
  'Prado', 'Queiroz', 'Ramos', 'Rezende', 'Ribeiro', 'Rocha', 'Rodrigues', 'Sampaio',
  'Santana', 'Santos', 'Silva', 'Soares', 'Souza', 'Teixeira', 'Vieira'
];

const productIds = Array.from({ length: 50 }, (_, index) => index + 1001);

function pick(arr) {
  return arr[Math.floor(Math.random() * arr.length)];
}

function shuffle(arr) {
  for (let i = arr.length - 1; i > 0; i -= 1) {
    const j = Math.floor(Math.random() * (i + 1));
    [arr[i], arr[j]] = [arr[j], arr[i]];
  }
  return arr;
}

function randomName() {
  return `${pick(firstNames)} ${pick(lastNames)}`;
}

function randomAmount() {
  const value = 9.9 + Math.random() * 490;
  return Number(value.toFixed(2));
}

function randomQuantity() {
  return Math.floor(Math.random() * 10) + 1;
}

function generateStore(index, location) {
  return {
    id: index,
    name: `Store-${location.state}-${String(index).padStart(3, '0')}`,
    city: location.city,
    state: location.state,
    country: location.country
  };
}

function generateStores(count) {
  const locations = shuffle([...cityStateCountry]);

  return Array.from({ length: count }).map((_, idx) => {
    const location = locations[idx % locations.length];
    return generateStore(idx + 1, location);
  });
}

function generateSalesman(index, stores) {
  const store = pick(stores);
  return {
    id: index,
    name: randomName(),
    store_id: store.id
  };
}

function generateSalesmen(count, stores) {
  return Array.from({ length: count }).map((_, index) => generateSalesman(index + 1, stores));
}

function generateSale(index, salesmen) {
  const salesman = pick(salesmen);
  return {
    id: index,
    salesman_id: salesman.id,
    store_id: salesman.store_id,
    amount: randomAmount(),
    sale_date: new Date().toISOString(),
    product_id: pick(productIds),
    quantity: randomQuantity()
  };
}

async function publishBatch(producer, topic, records) {
  if (!records.length) {
    return;
  }

  await producer.send({
    topic,
    messages: records.map((record) => ({
      key: String(record.id),
      value: JSON.stringify(record)
    }))
  });
}

async function main() {
  if (!config.brokers.length) {
    throw new Error('No Kafka brokers configured. Set KAFKA_BROKERS.');
  }

  if (config.salesPerSecond <= 0) {
    throw new Error('SALES_PER_SECOND must be greater than zero.');
  }

  if (config.salesFsPerSecond <= 0 || config.salesApiPerSecond <= 0) {
    throw new Error('SALES_FS_PER_SECOND and SALES_API_PER_SECOND must be greater than zero.');
  }

  if (config.salesFsPerSecond >= config.salesPerSecond || config.salesApiPerSecond >= config.salesPerSecond) {
    throw new Error('SALES_FS_PER_SECOND and SALES_API_PER_SECOND must be lower than SALES_PER_SECOND.');
  }

  const kafka = new Kafka({
    clientId: config.clientId,
    brokers: config.brokers
  });

  const producer = kafka.producer();

  const stores = generateStores(config.storeCount);
  const salesmen = generateSalesmen(config.salesmanCount, stores);
  let storeSequence = stores.length + 1;
  let salesmanSequence = salesmen.length + 1;
  let saleSequence = 1;

  const shutdown = async (signal) => {
    try {
      console.log(`\\nReceived ${signal}. Disconnecting producer...`);
      await producer.disconnect();
    } catch (err) {
      console.error('Error while disconnecting producer:', err);
    } finally {
      process.exit(0);
    }
  };

  process.on('SIGINT', () => shutdown('SIGINT'));
  process.on('SIGTERM', () => shutdown('SIGTERM'));

  await producer.connect();

  console.log('Publishing bootstrap data...');
  await publishBatch(producer, config.topicSalesFs, stores);
  await publishBatch(producer, config.topicSalesApi, salesmen);
  console.log(
    `Bootstrap complete: ${stores.length} stores -> ${config.topicSalesFs}, ${salesmen.length} salesmen -> ${config.topicSalesApi}`
  );

  const intervalMs = Math.max(1, Math.floor(1000 / config.salesPerSecond));
  const fsIntervalMs = Math.max(1, Math.floor(1000 / config.salesFsPerSecond));
  const apiIntervalMs = Math.max(1, Math.floor(1000 / config.salesApiPerSecond));

  console.log(
    `Streaming ${config.topicSalesDb} at ~${config.salesPerSecond} msg/s (interval ${intervalMs}ms)`
  );
  console.log(
    `Streaming ${config.topicSalesFs} at ~${config.salesFsPerSecond} msg/s (interval ${fsIntervalMs}ms)`
  );
  console.log(
    `Streaming ${config.topicSalesApi} at ~${config.salesApiPerSecond} msg/s (interval ${apiIntervalMs}ms)`
  );

  setInterval(async () => {
    const location = pick(cityStateCountry);
    const store = generateStore(storeSequence, location);
    storeSequence += 1;
    stores.push(store);

    try {
      await publishBatch(producer, config.topicSalesFs, [store]);
    } catch (err) {
      console.error('Failed to publish store event:', err.message || err);
    }
  }, fsIntervalMs);

  setInterval(async () => {
    const salesman = generateSalesman(salesmanSequence, stores);
    salesmanSequence += 1;
    salesmen.push(salesman);

    try {
      await publishBatch(producer, config.topicSalesApi, [salesman]);
    } catch (err) {
      console.error('Failed to publish salesman event:', err.message || err);
    }
  }, apiIntervalMs);

  setInterval(async () => {
    const sale = generateSale(saleSequence, salesmen);
    saleSequence += 1;

    try {
      await publishBatch(producer, config.topicSalesDb, [sale]);
    } catch (err) {
      console.error('Failed to publish sale event:', err.message || err);
    }
  }, intervalMs);
}

main().catch((err) => {
  console.error('Generator failed:', err);
  process.exit(1);
});
