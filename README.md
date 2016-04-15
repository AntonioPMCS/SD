# Projeto de Sistemas Distribuídos 2015-2016 #

Grupo de SD .49 - Campus Alameda

58803 Gonçalo Gaspar

79715 António Silva

70012 Filipe Cruzinha

Repositório:
[tecnico-distsys/C_49-project](https://github.com/tecnico-distsys/C_49-project/)

-------------------------------------------------------------------------------

## Instruções de instalação 


### Ambiente

[0] Iniciar sistema operativo

Linux


[1] Iniciar servidores de apoio

JUDDI:
```
cd juddi/bin
./startup.sh
```


[2] Criar pasta temporária

```
cd ~/
mkdir project
```


[3] Obter código fonte do projeto (versão entregue)

```
git clone https://github.com/tecnico-distsys/A_49-project.git
```


[4] Instalar módulos de bibliotecas auxiliares

```
cd uddi-naming
mvn clean install
```

```
cd ...
mvn clean install
```


-------------------------------------------------------------------------------

### Serviço TRANSPORTER

[1] Construir e executar **servidor**

```
cd transporter-ws
mvn clean install
mvn exec:java -Dws.i=x //'x' refere-se à instância que se deseja lançar
		       //ex: mxn exec:java -Dws.i=1
		       //default -> x=1
mvn test               //para correr testes unitários
```

[2] Construir **cliente** e executar testes

```
cd transporter-ws-cli
mvn clean install
mvn exec:java       //para correr situação exempo
mvn verify          //para correr testes integração e de mock
		    //ATENÇÃO: Lançar previamente dois Servidores Transporter-WS
		    //com as seguintes flags:
		    // -Dws.i=1
                    // -Dws.i=2
```

...


-------------------------------------------------------------------------------

### Serviço BROKER

[1] Construir e executar **servidor**

```
cd broker-ws
mvn clean install
mvn exec:java
mvn test	//para correr testes unitários simples
```


[2] Construir **cliente** e executar testes

```
cd broker-ws-cli
mvn clean install
mvn exec:java	//seguir instruções na command interface
mvn verify      //para correr testes integração
		//ATENÇÃO: Lançar previamente dois servidores transporter-WS
	   	//com as seguintes flags:
	   	// -Dws.i=1
           	// -Dws.i=2
		//Lançar também um servidor broker-ws
```

...

-------------------------------------------------------------------------------
**FIM**
