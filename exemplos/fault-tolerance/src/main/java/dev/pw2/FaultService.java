/**
 * PW2 by Rodrigo Prestes Machado
 *
 * PW2 is licensed under a
 * Creative Commons Attribution 4.0 International License.
 * You should have received a copy of the license along with this
 * work. If not, see <http://creativecommons.org/licenses/by/4.0/>.
 */
package dev.pw2;

import java.util.concurrent.Future;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.faulttolerance.Asynchronous;
import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.jboss.resteasy.specimpl.ResponseBuilderImpl;

@Path("/fault")
public class FaultService {

    /* Coloca no console as mensagens de erro */
    private static final Logger LOGGER = Logger.getLogger(FaultService.class.getName());

    private static final String FALL_BACK_MESSAGE = "FallbackMethod (posso mudar a mensagem aqui): ";

    @GET
    @Path("/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    @Retry(maxRetries = 3, delay = 2000)
    @Fallback(fallbackMethod = "recover")
    @Timeout(7000)
    public String getName(@PathParam("name") String name) {

        // Use esse trecho para simular um timeout
        //
        // try {
        // this.sleep();
        // } catch (InterruptedException e) {
        // LOGGER.info("Timeout");
        // }

        if (name.equalsIgnoreCase("error")) {
            System.out.println("testando!"); //ir no thunderclient, GET http://localhost:8080/fault/error e perceber que o "testando" aparece 4x
            ResponseBuilderImpl builder = new ResponseBuilderImpl();
            builder.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity("The requested was an error");
            Response response = builder.build();
            throw new WebApplicationException(response);
        }

        return name;
    }

    /**
     * Método usado para se recuperar de uma falha
     *
     * @param name O valor da url
     * @return uma mensagem de erro juntamente com o parâmetro de entrada
     */
    public String recover(String name) {
        return FALL_BACK_MESSAGE + name;
    }

    /**
     * Para testar o Bulkhead instale a ferramenta k6:
     *
     * https://k6.io/docs/
     *
     * Logo, na raiz desse projeto execute o comando:
     *
     * k6 run k6.js
     *
     * @param name
     * @return
     */
    @GET
    @Path("/bulkhead/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    @Asynchronous //adicionando esse Asynchronous para poder testar, comose fosse a Promise do JavaScript 
    @Bulkhead(value=5, waitingTaskQueue = 5)//indica que o limite de até duas requisições simultêneas é permitido, adicionais serão descartadas, pode-se usar o parâmetro "waitingTaskQueue" para liitar também o nº de requisições esperando a liberação
 /*    public String bulkhead(@PathParam("name") String name) {
        LOGGER.info(name);
        return name;
    }
 */ 

 public Future<String> bulkhead(@PathParam("name") Future<String> name){
    return name;
 } //situações que se usaria o bulkhead --> quando preciso limitar o número de requisições de alguma forma, limitar requisições grandes
 
    /**
     * Interrompe a thread por 10 segundos
     *
     * @throws InterruptedException
     */
    private void sleep() throws InterruptedException {
        Thread.sleep(10000);
    }


    /**Exemplo sobre o K6 --> é necessário ter o node.js instalado. Ir no diretório k6.js aqui deste projeto! ^-^  --> instalar o k6, seguir os passos da documentação k6.io, depois dentro da pasta executar o comando "k6 run k6.js*/

}
