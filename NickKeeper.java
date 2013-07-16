import com.coldsteelstudios.irc.*;

public class NickKeeper implements Plugin {
	public NickKeeper(){}

	public void init(Client c) {
		Connection irc = c.getConnection();
		irc.addMessageHandler(keeper).addCode(MessageCode.RPL_WELCOME);
		irc.addMessageHandler(keeper).addType(MessageType.NICKCHANGE);
	}

	private MessageHandler keeper = new MessageHandler() {
		public void handle(MessageEvent e) {
			Connection irc = e.getSource();

			if ( irc.nick() == irc.defaultNick() ) 
				return;

			irc.nick(irc.defaultNick());
		}
	};
}
