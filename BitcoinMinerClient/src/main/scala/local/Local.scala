package local

import akka.actor._
import common._
import scala.util.Random
import java.security.MessageDigest
import akka.routing.RoundRobinRouter
import java.net.InetAddress
import java.security.MessageDigest

case class RemoteDetail(remoteActorString : String)

  


object Local {

	def main(args: Array[String]){
	
	println(" Scala Version : "+util.Properties.versionString)
println("Argument 0 :"+ args(0))
val serverIP = args(0)
val serverPort = "5150"

  implicit val system = ActorSystem("LocalSystem")
  val localActor = system.actorOf(Props(new LocalActor(serverIP,serverPort)), name = "LocalActor")  // the local actor
  localActor ! Start                                                       // start the action

	}

}



class Worker extends Actor {
	
		def calculateBtc(start: Int, nrOfElements: Int,compString: String): java.util.ArrayList[BitCoin] = {
			var coinList = new java.util.ArrayList[BitCoin]
			var password2 = "nvadyala" + Random.nextInt
			//println("password :"+pautil.Properties.versionStringssword2)
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
			   // 	println(BitCoin(password,sb.toString()))
			    } 
			}	
		coinList	
		}
 
		def receive = {
			case WorkN(start, nrOfElements,compString) ⇒
			sender ! Result(calculateBtc(start, nrOfElements,compString)) // perform the work
		}
	}
 
	class LocalMaster(nrOfWorkers: Int, nrOfMessages: Int, nrOfElements: Int,compString:String, listener: ActorRef)
	extends Actor {
 
		var hashval: java.util.ArrayList[BitCoin] = _ 
		var nrOfResults: Int = _
		val start: Long = System.currentTimeMillis
 
		val workerRouter = context.actorOf(
		Props[Worker].withRouter(RoundRobinRouter(nrOfWorkers)), name = "workerRouter")
 
		def receive = {
			case Calculate ⇒
			println("Calculate nrOfWorkers:"+nrOfWorkers+" nrOfMessages : "+nrOfMessages)
				for (i ← 0 until nrOfMessages) workerRouter ! WorkN(i * nrOfElements, nrOfElements,compString)
			case Result(value) ⇒
				hashval = value
				
				listener ! RemoteResult(hashval)
				nrOfResults += 1
			/*	if (nrOfResults == nrOfMessages) {
					println("Shutting down the client")
					listener ! Message("Shutting down client"+InetAddress.getLocalHost.getHostName)
					context.system.shutdown() 
					context.stop(self)  
				} */
			}
		}

	
	
	
	class LocalActor(masterIP: String , masterPort: String) extends Actor {


  // create the remote actor
val remoteActorString = "akka.tcp://BtcMasterSystem@"+masterIP+":"+masterPort+"/user/MasterActor"

println(remoteActorString)

val remote = context.actorFor(remoteActorString)

  var counter = 0

  def receive = {
    case RemoteDetail(remoteActorString) =>
	println("Details recieved : "+remoteActorString)
	val remote2 = context.actorFor(remoteActorString)
	println("sending bind request to remote")
	remote ! BindRequest
   case Start =>
        remote ! Message("Hello from the LocalActor")
        remote ! BindRequest 
    case BindOK =>
        sender ! RequestWork
    case Work(k,nrOfMessages, nrOfElements,compString,prefix) =>
	println(s"Work recieved :  numner of worers : '$k'  nrOfMessages = '$nrOfMessages' nrOfElements = $nrOfElements and compString = $compString from "+sender)

	
	val LocalMaster = context.actorOf(Props(new LocalMaster(
				k, nrOfMessages, nrOfElements,compString, self)),
				name = "LocalMaster")
 
		LocalMaster ! Calculate

	
     //   sender ! WorkResult(calculateBtcFor2(start, nrOfElements,compString,prefix),prefix)
    case WorkResultRecieved =>
        println("Client Work completed and recieved by MasterActor")
        println("Client shutting down now!!!")
        context.system.shutdown()
    case RemoteResult(hashval) =>
    		remote ! RemoteResult(hashval)
    case Message(msg) => 
        println(s"LocalActor received message: '$msg'")
        if (counter < 5) {
            sender ! Message("Hello back to you")
            counter += 1
        }
    case _ =>
        println("LocalActor got something unexpected.")
  }

}


