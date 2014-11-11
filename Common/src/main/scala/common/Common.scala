package common


case object Start // local sends to local to start
case object BindRequest // local worker sends to master
case object BindOK
case object RequestWork
case object WorkResultRecieved
case class Work(nrOfWorkers:Int,nrOfMessages: Int, nrOfElements: Int,compString: String,prefix: String) 
case class Message(msg: String)
case class BitCoin(password:String,hash: String)
case class BtcCoins(hash: java.util.ArrayList[BitCoin])
case class WorkResult(value2: java.util.ArrayList[BitCoin],prefix: String) 


	sealed trait BtcMessage
	case object Calculate extends BtcMessage
	case class WorkN(start: Int, nrOfElements: Int,compString: String) extends BtcMessage
	case class Result(value: java.util.ArrayList[BitCoin]) extends BtcMessage
	case class RemoteResult(value: java.util.ArrayList[BitCoin]) extends BtcMessage
	case class ShutdownMaster(message: String)
	
