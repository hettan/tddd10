package communication;

/*
 * Use this factory instead of using "new ..."
 * This will make it easier for us to change
 * the implementation later.
 */
public class CommunicationFactory {

	public static CommunicationDevice createCommunicationDevice()
	{
		return new CommunicationDeviceTemporaryImpl();
	}
}
