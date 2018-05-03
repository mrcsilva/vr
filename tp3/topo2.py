#!/usr/bin/python

from mininet.net import Mininet
from mininet.node import Controller, RemoteController, OVSController
from mininet.node import CPULimitedHost, Host, Node
from mininet.node import OVSKernelSwitch, UserSwitch
from mininet.node import IVSSwitch
from mininet.cli import CLI
from mininet.log import setLogLevel, info, error
from mininet.link import TCLink, Intf
from subprocess import call
from mininet.util import quietRun

def checkIntf( intf ):
    "Make sure intf exists and is not configured."
    config = quietRun( 'ifconfig %s 2>/dev/null' % intf, shell=True )
    if not config:
        error( 'Error:', intf, 'does not exist!\n' )
        exit( 1 )
    ips = re.findall( r'\d+\.\d+\.\d+\.\d+', config )
    if ips:
        error( 'Error:', intf, 'has an IP address,'
               'and is probably in use!\n' )
        exit( 1 )

def myNetwork():

    net = Mininet( topo=None,
                   build=False)

    info( '*** Adding controller\n' )
    c0=net.addController(name='c0', controller=RemoteController, protocol='tcp', ip='127.0.0.1', port=6653)

    info( '*** Add switches\n')
    s1 = net.addSwitch('s1', cls=OVSKernelSwitch)
    s2 = net.addSwitch('s2', cls=OVSKernelSwitch)
    s3 = net.addSwitch('s3', cls=OVSKernelSwitch)
    s4 = net.addSwitch('s4', cls=OVSKernelSwitch)
    s5 = net.addSwitch('s5', cls=OVSKernelSwitch)
    s6 = net.addSwitch('s6', cls=OVSKernelSwitch)
    s7 = net.addSwitch('s7', cls=OVSKernelSwitch)
    s8 = net.addSwitch('s8', cls=OVSKernelSwitch)
    s9 = net.addSwitch('s9', cls=OVSKernelSwitch)

    info( '*** Add hosts\n')
    c1 = net.addHost('c1', cls=Host, ip='10.0.0.1', defaultRoute=None)
    c2 = net.addHost('c2', cls=Host, ip='10.0.0.2', defaultRoute=None)
    dns1 = net.addHost('dns1', cls=Host, ip='10.0.0.20', defaultRoute=None)
    dns2 = net.addHost('dns2', cls=Host, ip='10.0.0.21', defaultRoute=None)
    fs1 = net.addHost('fs1', cls=Host, ip='10.0.0.22', defaultRoute=None)
    fs2 = net.addHost('fs2', cls=Host, ip='10.0.0.23', defaultRoute=None)

    info( '*** Add links\n')
    net.addLink(fs1, s1)
    net.addLink(fs1, s1)
    net.addLink(fs2, s1)
    net.addLink(fs2, s1)
    net.addLink(c1, s2)
    net.addLink(dns1, s3)
    net.addLink(dns1, s3)
    net.addLink(dns2, s7)
    net.addLink(dns2, s7)
    net.addLink(c2, s9)
    net.addLink(s1, s2)
    net.addLink(s1, s4)
    net.addLink(s2, s3)
    net.addLink(s2, s5)
    net.addLink(s3, s6)
    net.addLink(s4, s7)
    net.addLink(s4, s5)
    net.addLink(s5, s6)
    net.addLink(s5, s8)
    net.addLink(s6, s9)
    net.addLink(s7, s8)
    net.addLink(s8, s9)

    info('*** Changing IPs for anycast')
    dns1.setMAC('00:00:00:00:00:01', intf='dns1-eth0')
    dns1.setIP('10.0.0.20', prefixLen=8, intf='dns1-eth0')
    dns1.setMAC('00:00:00:00:00:11', intf='dns1-eth1')
    dns1.setIP('10.0.0.250', prefixLen=8, intf='dns1-eth1')

    dns2.setMAC('00:00:00:00:00:02', intf='dns2-eth0')
    dns2.setIP('10.0.0.21', prefixLen=8, intf='dns2-eth0')
    dns2.setMAC('00:00:00:00:00:12', intf='dns2-eth1')
    dns2.setIP('10.0.0.250', prefixLen=8, intf='dns2-eth1')

    fs1.setMAC('00:00:00:00:00:03', intf='fs1-eth0')
    fs1.setIP('10.0.0.22', prefixLen=8, intf='fs1-eth0')
    fs1.setMAC('00:00:00:00:00:13', intf='fs1-eth1')
    fs1.setIP('10.0.0.251', prefixLen=8, intf='fs1-eth1')

    fs2.setMAC('00:00:00:00:00:04', intf='fs2-eth0')
    fs2.setIP('10.0.0.23', prefixLen=8, intf='fs2-eth0')
    fs2.setMAC('00:00:00:00:00:14', intf='fs2-eth1')
    fs2.setIP('10.0.0.251', prefixLen=8, intf='fs2-eth1')

    info( '*** Starting network\n')
    net.build()
    info( '*** Starting controllers\n')
    for controller in net.controllers:
        controller.start()

    info( '*** Starting switches\n')
    net.get('s1').start([c0])
    net.get('s2').start([c0])
    net.get('s3').start([c0])
    net.get('s4').start([c0])
    net.get('s5').start([c0])
    net.get('s6').start([c0])
    net.get('s7').start([c0])
    net.get('s8').start([c0])
    net.get('s9').start([c0])

    info( '*** Post configure switches and hosts\n')

    CLI(net)
    net.stop()

if __name__ == '__main__':
    setLogLevel( 'info' )
    myNetwork()
