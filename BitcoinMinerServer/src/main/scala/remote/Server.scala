package remote

import akka.actor._
import common._
import scala.util.Random

import java.security.MessageDigest
import akka.routing.RoundRobinRouter

object HelloRemote  {
 
def main(args : Array[String]){
 println("Scala version :: "+util.Properties.versionString)
 var num  = java.lang.Integer.parseInt(args(0))
   val compBuffer = new StringBuffer()
    for(idx <- 1 to num){
     compBuffer.append('0')
   }
  
  val compString = compBuffer.toString()
  
  val nrOfWorkers = 6
  val nrOfElements = 1000000
  val nrOfMessages = 100


val system = ActorSystem("BtcMasterSystem")
val listener = system.actorOf(Props[Listener], name = "listener")
val masterActor = system.actorOf(Props(new Master(
				nrOfWorkers, nrOfMessages, nrOfElements,compString, listener)),
				name = "MasterActor")
 
				masterActor ! Calculate

  masterActor ! Message("The Master is alive and started")
  masterActor ! Start
}
}

class Worker extends Actor {
	
		def calculateBtc(start: Int, nrOfElements: Int,compString: String): java.util.ArrayList[BitCoin] = {
			var coinList = new java.util.ArrayList[BitCoin]
			var password2 = "nvadyala" + Random.nextInt
			for ( k <- start until (start + nrOfElements)) {
		        var password = password2 + k
			
			    val md = MessageDigest.getInstance("SHA-256")
			    md.update(password.getBytes)
			    val byteData = md.digest()
			    
			    var sb = new StringBuffer()
			    for (i <- 0 until byteData.length) {
					sb.append(java.lang.Integer.toString((byteData(i) & 0xff) + 0x100, 16)
			        .substring(1))
			    }
			    
			    if(new java.lang.String(sb.toString).startsWith(compString)) {
			    	coinList.add(BitCoin(password,sb.toString()))
			    } 
			}	
		coinList	
		}
 
		def receive = {
			case WorkN(start, nrOfElements,compString) ⇒
			sender ! Result(calculateBtc(start, nrOfElements,compString)) // perform the work
		}
	}
 
	class Master(nrOfWorkers: Int, nrOfMessages: Int, nrOfElements: Int,compString:String, listener: ActorRef)
	extends Actor {
 
		var hashval: java.util.ArrayList[BitCoin] = _ 
		var nrOfResults: Int = _
		var nrOfClients: Int = _
		val start: Long = System.currentTimeMillis
 
		val workerRouter = context.actorOf(
		Props[Worker].withRouter(RoundRobinRouter(nrOfWorkers)), name = "workerRouter")
 
		def receive = {
			case Calculate ⇒
				for (i ← 0 until nrOfMessages) workerRouter ! WorkN(i * nrOfElements, nrOfElements,compString)
			case Result(value) ⇒
				hashval = value
				listener ! BtcCoins(hashval)
				nrOfResults += 1
				if (nrOfResults == nrOfMessages) {
				println("Master should shutDown")
				//	listener ! ShutdownMaster("Good Bye")
				//	context.stop(self)
				} 
			case RemoteResult(value) =>
				println("RemoteResultRecieved")
				println("RemoteResultRecieved")
				println("RemoteResultRecieved")
				println("RemoteResultRecieved")
				listener ! BtcCoins(value)
				sender ! WorkResultRecieved
				
			 case Start =>
    				println("Master starting work")
   			 case BindRequest =>
				println("Bind request recieved ")
       		 sender ! BindOK
				println("Bind ok sent")
   			 case RequestWork =>
   				 var prefix: String =  "nvadyala" +Random.nextInt + Random.nextPrintableChar
				 println("Work request recieved")
                     sender ! Work(nrOfWorkers,nrOfMessages,nrOfElements,compString,prefix)
                
                case Message(msg) =>
                  println(s"Master received message "+msg)    	
				
			}
		}
 
	class Listener extends Actor {
		def receive = {
	
		case BtcCoins(hashval) ⇒
			
			if(hashval.size() != 0) {
				for(ix <- 0 until hashval.size()){
					println("String: %s\tHash: %s".format(hashval.get(ix).password, hashval.get(ix).hash))
				}
			}  
		
		case ShutdownMaster(message) ⇒
			println("\n\tShutdown MEssage \t%s"
			.format(message))
			context.system.shutdown() 
		}
	}


