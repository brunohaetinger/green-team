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

export { generateStores, generateSalesmen, generateSale };
