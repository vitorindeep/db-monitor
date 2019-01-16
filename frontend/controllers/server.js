var http = require('http')
var url = require('url')
var fs = require('fs')
var pug = require('pug')
var axios = require('axios')

var estilo = /style\.css/

http.createServer((req,res)=>{
	var purl = url.parse(req.url)
	if(purl.pathname == '/'){
		res.writeHead(200, {'Content-Type': 'text/html'})
		res.write(pug.renderFile('../view/index.pug'))
		res.end()
	}
	else if(purl.pathname == '/datafiles'){
		res.writeHead(200, {'Content-Type': 'text/html'})
		axios.get('http://localhost:8585/ords/monitor/datafiles/df')
			.then(resposta => {
				res.write(pug.renderFile('../view/datafiles.pug', {lista: resposta.data.items}))
				res.end()
			})
			.catch(erro => {
				console.log('Erro ao ler JSON da API REST.')
				res.write(pug.renderFile('../view/error.pug', {error: 'Erro de render', message: 'Erro ao ler JSON da API REST.'}))
				res.end()
			})
	}
	else if(purl.pathname == '/cpuhist'){
		res.writeHead(200, {'Content-Type': 'text/html'})
		axios.get('http://localhost:8585/ords/monitor/cpu/cpu')
			.then(resposta => {
				res.write(pug.renderFile('../view/cpu.pug', {lista: resposta.data.items}))
				res.end()
			})
			.catch(erro => {
				console.log('Erro ao ler JSON da API REST.')
				res.write(pug.renderFile('../view/error.pug', {error: 'Erro de render', message: 'Erro ao ler JSON da API REST.'}))
				res.end()
			})
	}else if(purl.pathname == '/cpu'){
		res.writeHead(200, {'Content-Type': 'text/html'})
		axios.get('http://localhost:8585/ords/monitor/cpu/cpu')
			.then(resposta => {
				lista = []
				for(i=0;i<resposta.data.items.length;i++){
					if(i%5==2)
						lista.push('[\''+resposta.data.items[i].username+'\','+resposta.data.items[i].cpuusage+']')
					else
						lista.push('[\'\','+resposta.data.items[i].cpuusage+']')
				}
				res.write(pug.renderFile('../view/chart.pug', {lista: lista,nome: '\'CPU Usage\'',label:'\'Units\''}))
				res.end()
			})
			.catch(erro => {
				console.log('Erro ao ler JSON da API REST.')
				res.write(pug.renderFile('../view/error.pug', {error: 'Erro de render', message: 'Erro ao ler JSON da API REST.'}))
				res.end()
			})
	}
	else if(purl.pathname == '/pgahist'){
		res.writeHead(200, {'Content-Type': 'text/html'})
		axios.get('http://localhost:8585/ords/monitor/pga/pga')
			.then(resposta => {
				res.write(pug.renderFile('../view/pga.pug', {lista: resposta.data.items}))
				res.end()
			})
			.catch(erro => {
				console.log('Erro ao ler JSON da API REST.')
				res.write(pug.renderFile('../view/error.pug', {error: 'Erro de render', message: 'Erro ao ler JSON da API REST.'}))
				res.end()
			})
	}
	else if(purl.pathname == '/pga'){
		res.writeHead(200, {'Content-Type': 'text/html'})
		axios.get('http://localhost:8585/ords/monitor/pga/pga')
			.then(resposta => {
				lista = []
				for(i=0;i<resposta.data.items.length;i++){
					if(i%5==2)
						lista.push('[\''+resposta.data.items[i].name+'\','+resposta.data.items[i].usedpga+']')
					else
						lista.push('[\'\','+resposta.data.items[i].usedpga+']')
				}
				res.write(pug.renderFile('../view/chart.pug', {lista: lista,nome: '\'Used PGA\'',label:'\'Bytes\''}))
				res.end()
			})
			.catch(erro => {
				console.log('Erro ao ler JSON da API REST.')
				res.write(pug.renderFile('../view/error.pug', {error: 'Erro de render', message: 'Erro ao ler JSON da API REST.'}))
				res.end()
			})
	}
	else if(purl.pathname == '/sgahist'){
		res.writeHead(200, {'Content-Type': 'text/html'})
		axios.get('http://localhost:8585/ords/monitor/sga/sga')
			.then(resposta => {
				res.write(pug.renderFile('../view/sga.pug', {lista: resposta.data.items}))
				res.end()
			})
			.catch(erro => {
				console.log('Erro ao ler JSON da API REST.')
				res.write(pug.renderFile('../view/error.pug', {error: 'Erro de render', message: 'Erro ao ler JSON da API REST.'}))
				res.end()
			})
	}
	else if(purl.pathname == '/sga'){
		res.writeHead(200, {'Content-Type': 'text/html'})
		axios.get('http://localhost:8585/ords/monitor/sga/sga')
			.then(resposta => {
				lista = []
				for(i=0;i<resposta.data.items.length;i++){
					if(i%5==2)
						lista.push('[\''+resposta.data.items[i].name+'\','+resposta.data.items[i].total+']')
					else
						lista.push('[\'\','+resposta.data.items[i].total+']')
				}
				res.write(pug.renderFile('../view/chart.pug', {lista: lista,nome: '\'Total SGA\'',label:'\'Bytes\''}))
				res.end()
			})
			.catch(erro => {
				console.log('Erro ao ler JSON da API REST.')
				res.write(pug.renderFile('../view/error.pug', {error: 'Erro de render', message: 'Erro ao ler JSON da API REST.'}))
				res.end()
			})
	}
	else if(purl.pathname == '/sessions'){
		res.writeHead(200, {'Content-Type': 'text/html'})
		axios.get('http://localhost:8585/ords/monitor/sessions/sessions')
			.then(resposta => {
				res.write(pug.renderFile('../view/sessions.pug', {lista: resposta.data.items}))
				res.end()
			})
			.catch(erro => {
				console.log('Erro ao ler JSON da API REST.')
				res.write(pug.renderFile('../view/error.pug', {error: 'Erro de render', message: 'Erro ao ler JSON da API REST.'}))
				res.end()
			})
	}
	else if(purl.pathname == '/tablespaces'){
		res.writeHead(200, {'Content-Type': 'text/html'})
		axios.get('http://localhost:8585/ords/monitor/tablespaces/tablespaces')
			.then(resposta => {
				res.write(pug.renderFile('../view/tablespaces.pug', {lista: resposta.data.items}))
				res.end()
			})
			.catch(erro => {
				console.log('Erro ao ler JSON da API REST.')
				res.write(pug.renderFile('../view/error.pug', {error: 'Erro de render', message: 'Erro ao ler JSON da API REST.'}))
				res.end()
			})
	}
	else if(purl.pathname == '/users'){
		res.writeHead(200, {'Content-Type': 'text/html'})
		axios.get('http://localhost:8585/ords/monitor/users/usr')
			.then(resposta => {
				res.write(pug.renderFile('../view/users.pug', {lista: resposta.data.items}))
				res.end()
			})
			.catch(erro => {
				console.log('Erro ao ler JSON da API REST.')
				res.write(pug.renderFile('../view/error.pug', {error: 'Erro de render', message: 'Erro ao ler JSON da API REST.'}))
				res.end()
			})
	}
	else if(estilo.test(purl.pathname)){
		res.writeHead(200, {'Content-Type': 'text/css'})
		fs.readFile('../stylesheets/style.css', (erro, dados)=>{
			if(!erro)
				res.write(dados)
			else
				res.write('<p><b>Erro: </b> ' + erro + '</p>')
			res.end()
		})
	}
	else {
		res.writeHead(200, {'Content-Type': 'text/html'})
		res.write(pug.renderFile('../view/error.pug', {error: 'Erro de acesso', message: 'Pedido desconhecido: '+purl.pathname}))
		res.end()
	}
}).listen(5000, ()=>{
	console.log('Servidor à escuta na porta 5000...')
})