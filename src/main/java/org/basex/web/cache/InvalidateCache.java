package org.basex.web.cache;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.basex.core.Context;
import org.basex.server.ClientSession;
import org.basex.server.EventNotifier;

/**
 * Handles invalidation sequences using the notifying mechanism of the event framework
 * 
 * @author Robert
 *
 */
public final class InvalidateCache {


	/** keeps track of the number of events */
	int increment = 0;

	/** used for removal of events from the database if the local hashmap 
	 * is empty after restart of the jetty server */

	int increment2 = 1;
	protected String fPath;

	/** array of invalidation strings */

	private ArrayList<String> invalidatestringsarray = new ArrayList<String>();

	/** Hashmap with invalidation strings and the corresponding events */

	Map<String, String> m = new HashMap<String, String>();

	/**
	 * Constructor
	 */
	private InvalidateCache() { }


	/** invalidation instance */

	private static InvalidateCache instance;


	/**
	 * Factory method.
	 * @return invalidation instance.
	 */

	public static synchronized InvalidateCache getInstance() {
		// System.out.println("Instance: " + instance);
		if(instance == null) {
			System.out.println("Creating a new invalidation instance");
			instance = new InvalidateCache();
		}
		return instance;
	}

	/**
	 * @param map unsorted hashmap  
	 * @return  parameterized list sorted by invalidation instances 
	 * 
	 */

	private static  <K, V extends  Comparable <?super V> >  List 
	<Map.Entry<K, V>> sortMapByValue (final Map<K, V> map)     
	{
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {

			@Override
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return o1.getValue().compareTo(o2.getValue());
			}
		});

		return list;
	}

	/**
	 *  removes all events from the database if the local hashmap 
	 *  is empty after restart of the jetty server 
	 */

	public void removeallevents () {
		try {

			final ClientSession clientsession =
					new ClientSession(new Context(),"admin", "admin", null);

			@SuppressWarnings("unused")
			boolean flag = false;

			String s = "invalidate";
			String concat = s + increment2; 

			if (m.isEmpty()){
				while( flag = clientsession.execute("drop event "+concat+"") !=null)
				{
					increment2++;
					concat = s + increment2; 
					clientsession.execute("drop event "+concat+"");
					increment2++;
					concat = s + increment2; 

				}

			}
		}


		catch(final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Main method.
	 * @param tobeinvalidated string to be invalidated
	 * 
	 */
	public void invalidatecache (String tobeinvalidated) {

		removeallevents();
		try {

			final ClientSession clientsession =
					new ClientSession(new Context(),"admin", "admin", null);

			System.out.println("Watching...");

			final ClientSession clientsession2 =
					new ClientSession(new Context(),"admin", "admin", null); 

			if  (!invalidatestringsarray.contains(tobeinvalidated)) {

				invalidatestringsarray.add(tobeinvalidated);
				increment++;

				System.out.println("Current entries in array of invalidation strings : ");
				for (String l : invalidatestringsarray)
					System.out.println(l);

				String s = "invalidate";
				String concat = s + increment; 

				clientsession.execute("create event "+concat+"");

				clientsession.watch(concat, new EventNotifier() {

					@Override
					public void notify(String value) {
						String valuereceived = value;
						System.err.println("VALUE RECEIVED: " +valuereceived);
						try {
							fPath = new File(valuereceived).getCanonicalPath();
						} catch(final IOException e) {
						}
						String file = "";
						final File f = new File(fPath, file);
						CacheKey cacheKey = new CacheKey(new FirstLayerCacheKey(f, null, null));
						try {
							cacheKey.invalidate();
						} catch(IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				});

				clientsession2.query("db:event('"+concat+"', '"+tobeinvalidated+"')").execute();

				m.put(tobeinvalidated, concat);

				List <Map.Entry<String, String>> list = sortMapByValue(m);

				for (Map.Entry<String, String> entry : list) {
					String key = entry.getKey();
					String value = entry.getValue();

					System.out.print("Invalidation String: " + key + " Event: " + value);
					System.out.println("");
				}
			}
			else {

				String currentvalue;
				currentvalue = m.get(tobeinvalidated);

				List <Map.Entry<String, String>> list = sortMapByValue(m);

				for (Map.Entry<String, String> entry : list) {
					String key = entry.getKey();
					String value = entry.getValue();

					System.out.print("Invalidation String: " + key + " Event: " + value);
					System.out.println("");
				}

				clientsession2.query("db:event('"+currentvalue+"', '"+tobeinvalidated+"')").execute();

			}

		} catch(final IOException e) {
			e.printStackTrace();
		}

	}

}
