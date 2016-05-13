# Projeto de Sistemas Distribuídos 2015-2016 #

Grupo de SD .49 - Campus Alameda

58803 Gonçalo Gaspar

79715 António Silva

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
cd uddi-naming -> UDDINaming1.1
mvn clean install
```


```
cd upa-library
mvn clean install
```

```
cd transporter-cli
mvn clean install
```

```
cd broker-cli
mvn clean install
```


-------------------------------------------------------------------------------
### Serviço CENTRAL AUTHORITY
[5] Construir e executar **servidor**
```
cd ca-ws
mvn clean compile
mvn exec:java
```

[6] Construir **cliente**  //ATENÇÃO: O servidor ca-ws fornece o WSDL por webservice, por isso tem de estar ligado)
```
cd ca-ws-cli
mvn clean install

```

-------------------------------------------------------------------------------

### Serviço TRANSPORTER

[7] Construir e executar **servidor par**

```
cd transporter-ws
mvn clean compile
mvn exec:java 
mvn test  -> para correr testes unitários
```
[8] Executar **servidor impar**

```
cd transporter-ws
mvn exec:java -Dws.i=2
```

[9] Construir **cliente** e executar testes

```
cd transporter-ws-cli
mvn clean compile
mvn exec:java       //para correr situação exemplo
mvn verify          //para correr testes integração e de mock
		    //ATENÇÃO: Lançar previamente dois Servidores Transporter-WS como indicado
```

-------------------------------------------------------------------------------

### Serviço BROKER

[10] Construir e executar **servidor secundário**

```
cd broker-ws
mvn clean compile
mvn exec:java -Dws.i=1
mvn test	//para correr testes unitários simples
```

[11] Executar **servidor primário**
```
cd broker-ws
mvn exec:java
```

[12] Construir **cliente** e executar testes

```
cd broker-ws-cli
mvn clean compile
mvn exec:java	//seguir instruções na command interface
mvn verify      //para correr testes integração
		//ATENÇÃO: Lançar previamente dois servidores transporter-WS e os dois servidores brokers como indicado
		//Lançar também um servidor broker-ws
```
...

-------------------------------------------------------------------------------
**FIM**
