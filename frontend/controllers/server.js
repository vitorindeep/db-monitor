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
				console.log(resposta.data.items)
				res.render('../view/datafiles.pug', {lista: resposta.data.items})
			})
			.catch(erro => {
				console.log('Erro ao ler JSON da API REST.')
				res.render('../view/erro.pug', {error: erro, message: 'Erro ao ler JSON da API REST.'})
			})
		res.end()
	}
	else if(estilo.test(purl.pathname)){
		res.writeHead(200, {'Content-Type': 'text/css'})
		fs.readFile('../stylesheets/style.css', (erro, dados)=>{
			console.log(purl.pathname)
			if(!erro)
				res.write(dados)
			else
				res.write('<p><b>Erro: </b> ' + erro + '</p>')
			res.end()
		})
	}
	else {
		res.writeHead(200, {'Content-Type': 'text/html'})
		res.write('<p><b>Erro, pedido desconhecido: </b> ' + purl.pathname + '</p>')
		res.end()
	}
}).listen(5000, ()=>{
	console.log('Servidor à escuta na porta 5000...')
})