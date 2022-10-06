import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class ARPLayer implements BaseLayer{

    public int nUpperLayerCount = 0;
    public String pLayerName = null;
    public BaseLayer p_UnderLayer = null;
    public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
    _ARP_MSG arp_header;
    
    // ARP Cache Table
    public ArrayList<_ARP_Cache> ArpCacheTable = new ArrayList<>();
    //Proxy Entry Table
    public ArrayList<_Proxy_Entry> ProxyEntryTable = new ArrayList<>();

    private class _IP_ADDR {
        private byte[] addr = new byte[4];

        public _IP_ADDR() {
            this.addr[0] = (byte) 0x00;
            this.addr[1] = (byte) 0x00;
            this.addr[2] = (byte) 0x00;
            this.addr[3] = (byte) 0x00;
        }
    }

    private class _ETHERNET_ADDR {
        private byte[] addr = new byte[6];

        public _ETHERNET_ADDR() {
            this.addr[0] = (byte) 0x00;
            this.addr[1] = (byte) 0x00;
            this.addr[2] = (byte) 0x00;
            this.addr[3] = (byte) 0x00;
            this.addr[4] = (byte) 0x00;
            this.addr[5] = (byte) 0x00;
        }
    }


    private class _ARP_MSG {
        byte[] hardType;                // 2bytes. Type of Hardware Address
        byte[] protType;                // 2bytes. Type of Protocol Address
        byte hardSize;                  // 1byte. (Ethernet - 6bytes)
        byte protSize;                  // 1byte. (IP - 4bytes)
        byte[] opCode;                  // 2bytes. [1 : ARP Request / 2 : ARP Reply]
        _ETHERNET_ADDR srcMacAddr;      //Sender's Ethernet Address(MAC주소 : 6bytes)
        _IP_ADDR srcIPAddr;             //Sender's IP Address(IP주소 : 4bytes)
        _ETHERNET_ADDR dstMacAddr;      // Target's Ethernet Address;(MAC주소 : 6bytes)
        _IP_ADDR dstIPAddr;             //Target's IP Address;(IP주소 : 4bytes)


        public _ARP_MSG() {
            this.hardType = new byte[2];
            this.protType = new byte[2];
            this.hardSize = (byte) 0x00;
            this.protSize = (byte) 0x00;
            this.opCode = new byte[2];
            this.srcMacAddr = new _ETHERNET_ADDR();
            this.srcIPAddr = new _IP_ADDR();
            this.dstMacAddr = new _ETHERNET_ADDR();
            this.dstIPAddr = new _IP_ADDR();
        }
    }
    
    // ARP MSG Reset 함수
    private void ResetMSG() {
    	arp_header = new _ARP_MSG();
    	arp_header.hardType[0] = (byte)0x00;
    	arp_header.hardType[1] = (byte)0x01;		// hardware Type은 0x01 고정
    	arp_header.protType[0] = (byte)0x08;
    	arp_header.protType[1] = (byte)0x06;		// ARP Type은 0x0806 (Chat/File 구현시 삭제)
    	arp_header.hardSize = (byte)0x06;			// Ethernet은 6bytes
    	arp_header.protSize = (byte)0x04;			// IPv4 사용하므로 4bytes
    	for(int i = 0; i < 6; i++) {
    		arp_header.dstMacAddr.addr[i] = (byte)0xff;		//Broadcast
    	}
    	
    }
    
    // ARP Layer 생성자
    public ARPLayer(String pName) {
    	pLayerName = pName;
    	ResetMSG();		// Layer 생성할 때 Reset 한다
    }
    
    // ARP Cache
    public class _ARP_Cache {
    	byte[] ipAddr;
    	byte[] macAddr;
    	boolean status;				// complete == true, incomplete == false
    	
    	public _ARP_Cache(byte[] ipAddr, byte[] macAddr, boolean status) {
    		this.ipAddr = ipAddr;
    		this.macAddr = macAddr;
    		this.status = status;
    	}
    }
    
    public class _Proxy_Entry {
    	String hostName;
    	byte[] ipAddr;
    	byte[] macAddr;
    	
    	public _Proxy_Entry (String hostName, byte[] ipAddr, byte[] macAddr) {
    		this.hostName = hostName;
    		this.ipAddr = ipAddr;
    		this.macAddr = macAddr;
    	}
    }


    public boolean Send(byte[] input, int length) {
        // Cache Entry Table에서 확인(있으면 거기로 전송, 없으면 캐시 추가)


        return true;
    }
    
    // GARP Send 함수
    public boolean GARPSend(byte[] input) {
    	
    	return true;
    	
    }

    // Reply Send할 때 쓸 함수
    public boolean ReplySend(byte[] request) {
        byte[] repMsg = new byte[28];
        //Request 그대로 복사
        System.arraycopy(request, 0, repMsg, 0, request.length);
        // opcode 2로 설정
        repMsg[7] = (byte)0x02;
        // 자신의 MAC 주소 Target Address에 입력
        System.arraycopy(arp_header.srcMacAddr, 0, request, 18, 6);
        
        //Sender & Target 정보 서로 변경
        System.arraycopy(request, 18, repMsg, 7, 10);	// Target정보를 Sender로 이동
        System.arraycopy(request, 7, repMsg, 18, 10);	// Sender 정보를 Target으로 이동
        
        // 하위 Layer로 내려보냄
        ((EthernetLayer)this.GetUnderLayer()).Send(repMsg, 28); //응답 메세지 전송

        return true;
    }

    // Receive 함수
    public boolean Receive(byte[] input) {

        

        return true;
    }

    // _ARP_MSG Object를 byte[]로 바꿔주는 함수
    public byte[] ObjToByte(_ARP_MSG arpMsg) {
        byte[] buf = new byte[28];
        
        buf[0] = arpMsg.hardType[0];
        buf[1] = arpMsg.hardType[1];
        buf[2] = arpMsg.protType[0];
        buf[3] = arpMsg.protType[1];
        buf[4] = arpMsg.hardSize;
        buf[5] = arpMsg.protSize;
        buf[6] = arpMsg.opCode[0];
        buf[7] = arpMsg.opCode[1];
        
        for(int i = 0 ; i < 6; i++) {
        	buf[i+8] = arpMsg.srcMacAddr.addr[0];
        }
        
        
        return buf;
    }

    // ARP Msg의 Target IP와 나의 IP 주소와 비교
    public boolean IsMyIP (byte[] targetIP) {
    	for(int i = 0; i < 4 ; i++) {
    		// 일치하지 않는 경우
    		if (arp_header.srcIPAddr.addr[i] != targetIP[i]) {
    			return false;
    		}
    	}
    	return true;
    }
    
    // Proxy Table에 있는지 확인하는 함수
    public boolean IsInProxyTable(byte[] targetIP) {
    	// iterator로 ArrayList를 순회
    	Iterator <_Proxy_Entry> iter = ProxyEntryTable.iterator();
    	while(iter.hasNext()) {
    		// targetIP와 Entry의 IP주소가 같은지 확인
    		if (Arrays.equals(targetIP, iter.next().ipAddr)){
    			return true;
    		}
    	}
    	return false;	//Proxy Table에 존재하지 않는 경우
    }

    
    
    @Override
    public void SetUnderLayer(BaseLayer pUnderLayer) {
        if (pUnderLayer == null)
            return;
        this.p_UnderLayer = pUnderLayer;
    }

    @Override
    public void SetUpperLayer(BaseLayer pUpperLayer) {
        if (pUpperLayer == null)
            return;
        this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);
    }

    @Override
    public String GetLayerName() {
        // TODO Auto-generated method stub
        return pLayerName;
    }

    @Override
    public BaseLayer GetUnderLayer() {
        if (p_UnderLayer == null)
            return null;
        return p_UnderLayer;
    }

    @Override
    public BaseLayer GetUpperLayer(int nindex) {
        if (nindex < 0 || nindex > nUpperLayerCount || nUpperLayerCount < 0)
            return null;
        return p_aUpperLayer.get(nindex);
    }

    @Override
    public void SetUpperUnderLayer(BaseLayer pUULayer) {
        this.SetUpperLayer(pUULayer);
        pUULayer.SetUnderLayer(this);
    }
}
