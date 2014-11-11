
 
import akka.actor._
import akka.routing.RoundRobinRouter
import akka.util.Duration
import akka.util.duration._
import scala.util.Random

import java.security.MessageDigest
 
object Btc  {

	def main(args: Array[String]) = {
		var num  = java.lang.Integer.parseInt(args(0))
		val compBuffer = new StringBuffer()
		for(idx <- 1 to num){
		compBuffer.append('0')
	}
	val compString = compBuffer.toString()  
 
	calculate(nrOfWorkers = 6, nrOfElements = 1000000, nrOfMessages = 100,compString)
 
	sealed trait BtcMessage
	case object Calculate extends BtcMessage
	case class Work(start: Int, nrOfElements: Int,compString: String) extends BtcMessage
	case class Result(value: java.util.ArrayList[BitCoin]) extends BtcMessage
	case class BtcCalculationTime(duration: Duration)
	case class BtcCoins(hash: java.util.ArrayList[BitCoin])
	case class BitCoin(password:String,hash: String)

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
			case Work(start, nrOfElements,compString) ⇒
			sender ! Result(calculateBtc(start, nrOfElements,compString)) // perform the work
		}
	}
 
	class Master(nrOfWorkers: Int, nrOfMessages: Int, nrOfElements: Int,compString:String, listener: ActorRef)
	extends Actor {
 
		var hashval: java.util.ArrayList[BitCoin] = _ 
		var nrOfResults: Int = _
		val start: Long = System.currentTimeMillis
 
		val workerRouter = context.actorOf(
		Props[Worker].withRouter(RoundRobinRouter(nrOfWorkers)), name = "workerRouter")
 
		def receive = {
			case Calculate ⇒
			for (i ← 0 until nrOfMessages) workerRouter ! Work(i * nrOfElements, nrOfElements,compString)
				case Result(value) ⇒
				hashval = value
				listener ! BtcCoins(hashval)
				nrOfResults += 1
				if (nrOfResults == nrOfMessages) {
					listener ! BtcCalculationTime(duration = (System.currentTimeMillis - start).millis)
					context.stop(self)
				}
			}
		}
 
	class Listener extends Actor {
		def receive = {
	
		case BtcCoins(hashval) ⇒
			if(hashval.size() != 0) {
				for(ix <- 0 until hashval.size()){
					println("String: %s\tHash: %s"
					.format(hashval.get(ix).password, hashval.get(ix).hash))
				}
			}  
		
		case BtcCalculationTime(duration) ⇒
			println("\n\tCalculation time: \t%s"
			.format(duration))
			context.system.shutdown() 
		}
	}
 
		def calculate(nrOfWorkers: Int, nrOfElements: Int, nrOfMessages: Int,compString: String) {
			val system = ActorSystem("BtcSystem")
			val listener = system.actorOf(Props[Listener], name = "listener")
			val master = system.actorOf(Props(new Master(
				nrOfWorkers, nrOfMessages, nrOfElements,compString, listener)),
				name = "master")
 
				master ! Calculate
		}
	}
}
