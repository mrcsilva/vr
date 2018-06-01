from mininet.topo import Topo
from mininet.node import Host

class TP3( Topo ):
    "Simple topology example."

    def __init__( self ):
        "Create custom topo."

        # Initialize topology
        Topo.__init__( self )

        # Add hosts and switches
        h1 = Host( 'h1' )
        self.addHost( h1 )
        h2 = self.addHost( 'h2' )
        h3 = self.addHost( 'h3' )
        s1 = self.addSwitch( 's1' )
        s2 = self.addSwitch( 's2' )
        h1.addIntf(self, 'h1-eth1')
        h1.setIP(self, '10.0.0.250', 8, 'h1-eth1')

        # Add links
        self.addLink( h1, s1 )
        self.addLink( s1, s2 )
        self.addLink( h2, s2 )
        self.addLink( h3, s2 )



topos = { 'mytopo': ( lambda: TP3() ) }
