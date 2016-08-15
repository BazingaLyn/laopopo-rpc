package org.laopopo.common.transport.body;

import org.laopopo.common.exception.remoting.RemotingCommmonCustomException;




public interface CommonCustomBody {
	
    void checkFields() throws RemotingCommmonCustomException;
}
