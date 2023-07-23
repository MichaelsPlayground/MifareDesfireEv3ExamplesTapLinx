createNdef
this script will generate a NDEF file on the DESFire EV3 tag
step 1: select Master Application 00 00 00
Command sent to card : 5A000000
Response received : 00
step 2: create new application 00 00 01
Command sent to card : CA       0100000F2110E1D2760000850101
Playground             ca00000e 0100000f2110e1d2760000850101 00
Response received : 00
step 3: select new application 00 00 01
Command sent to card : 5A       010000
Playground             5a000003 010000 00
Response received : 00
step 4: create a standard data file 01
Command sent to card : CD       0103E100EEEE0F0000
Playground             cd000009 0103e100eeee0f0000 00
Response received : 00
step 5: write to the standard data file 01 (NDEF container)
Command sent to card : F501
Response received : 000000EEEE0F0000
Command sent to card : 3D       010000000F0000000F20003A00340406E10401000000
Playground             3d000016 010000000f0000000f20003a00340406e10401000000 00
Response received : 00
step 6: create a standard data file 02
Command sent to card : CD       0204E100E0EE000100
Playground:            cd000009 0204e100e0ee000100 00

using SDM enabling
Command sent to card : CD       0204E140E0EE000100     ** this is with SDM enabling *** 
                                      40
Error received:        9D Did you forget to authenticate with a write access key ?
Response received : 00

step 7: write to the standard data file 02 (NDEF container)
Command sent to card : F502
Response received : 000000EEEE000100

Command sent to card : 3D       020000000200000000
Playground             3D000009 020000000200000000 00       
Response received : 00
generation of a NDEF file on the DESFire EV3 tag finished
createNdef: true
createNdef SUCCESS
