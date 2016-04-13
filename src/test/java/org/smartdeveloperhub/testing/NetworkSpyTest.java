/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the Smart Developer Hub Project:
 *     http://www.smartdeveloperhub.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2015-2016 Center for Open Middleware.
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Artifact    : org.smartdeveloperhub.vocabulary:sdh-vocabulary:0.3.0-SNAPSHOT
 *   Bundle      : sdh-vocabulary-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.testing;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.junit.Test;

public class NetworkSpyTest {

	@Test
	public void spy() throws SocketException {
		final Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
		while(networkInterfaces.hasMoreElements()) {
			final NetworkInterface niface=networkInterfaces.nextElement();
			System.out.printf("* Network interface: %s%n",niface);
			for(final InterfaceAddress ifaceAddress:niface.getInterfaceAddresses()) {
				System.out.printf("  - Interface address: %s%n",ifaceAddress);
				dumpAddress(ifaceAddress.getAddress());
			}
		}
	}

	private void dumpAddress(final InetAddress address) {
		System.out.printf("    * %s:%n",address);
		System.out.printf("      + Host address.......: %s%n",address.getHostAddress());
		System.out.printf("      + Host name..........: %s%n",address.getHostName());
		System.out.printf("      + Canonical host name: %s%n",address.getCanonicalHostName());
		System.out.printf("      + Is link local: %s%n",address.isLinkLocalAddress());
		System.out.printf("      + Is loopback..: %s%n",address.isLoopbackAddress());
		System.out.printf("      + Is any local.: %s%n",address.isAnyLocalAddress());
		System.out.printf("      + Is site local: %s%n",address.isSiteLocalAddress());
		System.out.printf("      + Is multicast.: %s%n",address.isMulticastAddress());
		try {
			System.out.printf("      + Is reachable.: %s%n",address.isReachable(5000));
		} catch (final IOException e) {
			System.out.printf("      + Is reachable.: %s%n",false);
		}
	}

}
