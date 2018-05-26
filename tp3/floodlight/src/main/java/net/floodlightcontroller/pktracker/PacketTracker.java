package net.floodlightcontroller.pktracker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.types.ArpOpcode;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.packet.ARP;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;

public class PacketTracker implements IOFMessageListener, IFloodlightModule {
	
	protected IFloodlightProviderService floodlightProvider;
	protected Set<Long> macAddresses;
	protected static Logger logger;
	protected int last = 1;
	
	@Override
	public String getName() {
	    return PacketTracker.class.getSimpleName();
	}
 
    @Override
    public boolean isCallbackOrderingPrereq(OFType type, String name) {
        // TODO Auto-generated method stub
        return false;
    }
 
    @Override
    public boolean isCallbackOrderingPostreq(OFType type, String name) {
        // TODO Auto-generated method stub
        return false;
    }
 
    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        // TODO Auto-generated method stub
        return null;
    }
 
    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        // TODO Auto-generated method stub
        return null;
    }
 
    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l =
            new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IFloodlightProviderService.class);
        return l;
    }
 
    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
        macAddresses = new ConcurrentSkipListSet<Long>();
        logger = LoggerFactory.getLogger(PacketTracker.class);
    }
 
    @Override
    public void startUp(FloodlightModuleContext context) {
        floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
    }
 
    @Override
    public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
    		Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
        
        switch (msg.getType()) {
        		case PACKET_IN:
     
        			/* Various getters and setters are exposed in Ethernet */
     
        			/* 
        			 * Check the ethertype of the Ethernet frame and retrieve the appropriate payload.
        			 * Note the shallow equality check. EthType caches and reuses instances for valid types.
        			 */
        			if(sw.getId().toString().equals("00:00:00:00:00:00:00:01")) {
        				if (eth.getEtherType() == EthType.IPv4) {
        					/* We got an IPv4 packet; get the payload from Ethernet */
        					IPv4 ipv4 = (IPv4) eth.getPayload();
	                 
        					/* Various getters and setters are exposed in IPv4 */
        					IPv4Address dstIp = ipv4.getDestinationAddress();
        					IPv4Address srcIp = ipv4.getSourceAddress();
        					
        					/* 
        					 * Check the IP protocol version of the IPv4 packet's payload.
        					 */
        					if (ipv4.getProtocol() == IpProtocol.TCP) {
        						logger.info("Received packet! Source IP: " + srcIp.toString() + "\nDestination IP: " + dstIp.toString() + "\nProtocol: TCP");
        					} 
        					else if (ipv4.getProtocol() == IpProtocol.UDP) {
        						logger.info("Received packet! Source IP: " + srcIp.toString() + "\nDestination IP: " + dstIp.toString() + "\nProtocol: UDP");
        					}
        					else if(ipv4.getProtocol() == IpProtocol.ICMP) {
        						logger.info("Received packet! Source IP: " + srcIp.toString() + "\nDestination IP: " + dstIp.toString() + "\nProtocol: ICMP");
        					}
        				} else if (eth.getEtherType() == EthType.ARP) {
        					/* We got an ARP packet; get the payload from Ethernet */
        					ARP arp = (ARP) eth.getPayload();
	                
        					MacAddress srcMac = arp.getSenderHardwareAddress();
        					MacAddress dstMac = arp.getTargetHardwareAddress();
	                
        					logger.info("Received ARP packet! Source MAC: " + srcMac.toString() + " Destination MAC: " + dstMac.toString());
	     
        				} 
        				else {
        					/* Unhandled ethertype */
        				}
        			}
        			if (eth.getEtherType() == EthType.IPv4) {
        				IPv4 ipv4 = (IPv4) eth.getPayload();
        				if(ipv4.getDestinationAddress().equals(IPv4Address.of("10.0.0.250")) && eth.getDestinationMACAddress().equals(MacAddress.of("ff:ff:ff:ff:ff:ff"))) {
        					
        					logger.info("CHANGING IP ADDRESS!");
        					logger.info("Received packet! Dest Mac: " + eth.getDestinationMACAddress().toString());
                    
        					MacAddress macsrc = eth.getSourceMACAddress();
        					MacAddress macdst = MacAddress.of("00:00:00:00:00:01");
        					IPv4Address ipDNS1 = IPv4Address.of("10.0.0.20");
        					IPv4Address ipDNS2 = IPv4Address.of("10.0.0.21");
                        
        					if(ipv4.getSourceAddress().equals(IPv4Address.of("10.0.0.1"))) {
        						if (last % 2 == 0) {
        							//ipv4.setDestinationAddress(ipDNS1);
        							macdst = MacAddress.of("00:00:00:00:00:01");
        						}
        						else {
                        			//ipv4.setDestinationAddress(ipDNS2);
                        			macdst = MacAddress.of("00:00:00:00:00:02");
                        		}
                        		last++;
        					}
        					else if(ipv4.getSourceAddress().equals(IPv4Address.of("10.0.0.2"))) {
        						//ipv4.setDestinationAddress(ipDNS1);
        						macdst = MacAddress.of("00:00:00:00:00:01");
        					}
        					
        					
        					Ethernet newEth = new Ethernet();
        					newEth.setDestinationMACAddress(macdst);
        					newEth.setSourceMACAddress(macsrc);
        					newEth.setPayload(ipv4);
        					newEth.setEtherType(EthType.IPv4);
        					logger.info("Switch: " + sw.getId().toString());
        					logger.info("MAC Dest: " + newEth.getDestinationMACAddress().toString() + "\nIP Dest: " + ((IPv4)newEth.getPayload()).getDestinationAddress().toString());
                        
        					OFFactory myFactory = sw.getOFFactory();
                    
        					/* Specify the switch port(s) which the packet should be sent out. */
        					OFActionOutput output = myFactory.actions().buildOutput()
        							.setPort(OFPort.FLOOD)
        							.build();
        					
                     
        					/* 
        					 * Compose the OFPacketOut with the above Ethernet packet as the 
        					 * payload/data, and the specified output port(s) as actions.
        					 */
        					OFPacketOut myPacketOut = myFactory.buildPacketOut()
        							.setData(newEth.serialize())
        							.setBufferId(OFBufferId.NO_BUFFER)
        							.setActions(Collections.singletonList((OFAction) output))
        							.build();
                     
        					/* Write the packet to the switch via an IOFSwitch instance. */
        					sw.write(myPacketOut);
        					return Command.STOP;
                }
            }
        		else if(eth.getEtherType() == EthType.ARP) {
        			ARP a = (ARP) eth.getPayload();
        			if(a.getTargetProtocolAddress().equals(IPv4Address.of("10.0.0.250"))) {
        				MacAddress macdst = MacAddress.of("ff:ff:ff:ff:ff:ff");
                    IPv4Address ipdst = IPv4Address.of("10.0.0.250");
                            
                    a.setTargetHardwareAddress(a.getSenderHardwareAddress());
                    a.setTargetProtocolAddress(a.getSenderProtocolAddress());
                    a.setSenderProtocolAddress(ipdst);
                    a.setSenderHardwareAddress(macdst);
                    
                    Ethernet newEth = new Ethernet();
                    
                    newEth.setDestinationMACAddress(a.getSenderHardwareAddress());
                    newEth.setSourceMACAddress(macdst);
                    newEth.setPayload(a);
                    newEth.setEtherType(EthType.ARP);
                    
                        
                    a.setOpCode(ArpOpcode.REPLY);
                    OFPacketOut.Builder pob = sw.getOFFactory().buildPacketOut();
                    
                    pob.setData(a.serialize());
                    
                    OFFactory myFactory = sw.getOFFactory();
                    
                    /* Specify the switch port(s) which the packet should be sent out. */
                    OFActionOutput output = myFactory.actions().buildOutput()
                        .setPort(OFPort.FLOOD)
                        .build();
                     
                    /* 
                     * Compose the OFPacketOut with the above Ethernet packet as the 
                     * payload/data, and the specified output port(s) as actions.
                     */
                    OFPacketOut myPacketOut = myFactory.buildPacketOut()
                        .setData(newEth.serialize())
                        .setBufferId(OFBufferId.NO_BUFFER)
                        .setActions(Collections.singletonList((OFAction) output))
                        .build();
                         
                    /* Write the packet to the switch via an IOFSwitch instance. */
                    sw.write(myPacketOut);
                    return Command.STOP;
        			        				
        			}
        		}
            break;
        default:
            break;
        }
        return Command.CONTINUE;
    }
	
}
