package org.laopopo.remoting.model;

import org.laopopo.remoting.exception.RemotingCommmonCustomException;



public interface CommonCustomHeader {
    void checkFields() throws RemotingCommmonCustomException;
}
