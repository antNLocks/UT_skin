$fn = 10000;

RX_x = [
    110,
    105,
    101,
    98,
    96,
    95,
    94,
    94,
    94,
    97,
    101,
    105,
    110,
    117,
    125,
    130,
    140,
    150,
    160,
    185,
    210,
    215,
    215,
    214,
    212,
    210,
    210,
    207,
    205,
    205,
    200,
    197,
    195,
    190,
    187,
    185];
    
RX_nailsOrientation = [
    1,
    1,
    1,
    1,
    1,
    1,
    1,
    1,
    1,
    1,
    1,
    1,
    1,
    1,
    1,
    1,
    1,
    1,
    1,
    -1,
    -1,
    -1,
    -1,
    -1,
    -1,
    -1,
    -1,
    -1,
    -1,
    -1,
    -1,
    -1,
    -1,
    -1,
    -1,
    -1,
    
];
    
TX_y = [
    -5,
    -5,
    -5,
    -5,
    -5,
    -5,
    -5,
    -5,
    125,
    125,
    155,
    155,
    175,
    175,
    195,
    195];

x_margin = 5;
x_border = 5;
nails_radius = 0.6;
nails_bigRadius = 1;


for(i = [0: 35.9]){
    //one RX nails left
    translate([RX_x[i] - x_margin - x_border - nails_bigRadius, -10*i +nails_bigRadius, 0])
        square([2*nails_radius, 2*nails_bigRadius], true);
    
    //all RX nails left
    translate([RX_x[i] - x_margin + nails_radius, -10*i, 0])
        square([2*nails_radius, 2*nails_radius], true);
    
    //one RX nails right
    translate([-RX_x[i] + x_margin + x_border + nails_radius, -10*i +nails_bigRadius, 0])
        square([2*nails_radius, 2*nails_bigRadius], true);
    
    //all RX nails left
    translate([-RX_x[i] + x_margin - nails_radius, -10*i, 0])
        square([2*nails_radius, 2*nails_radius], true);
    
    
}

for(i = [0: len(TX_y) - 1]){
    //one TX nails top right
    translate([10*i, - TX_y[i] - nails_bigRadius, 0])
        square([2*nails_radius, 2*nails_bigRadius], true);
    
    //one TX nails top left
     translate([-10*i, - TX_y[i] - nails_bigRadius, 0])
        square([2*nails_radius, 2*nails_bigRadius], true);
    
    //one TX nails bottom right
    translate([10*i - nails_bigRadius, -365, 0])
        square([2*nails_bigRadius, 2*nails_radius], true);
    
    //all TX nails bottom right
    translate([10*i, -370, 0])
        square([2*nails_radius, 2*nails_radius], true);
    
    //one TX nails bottom left
    translate([-10*i - nails_bigRadius, -365, 0])
        square([2*nails_bigRadius, 2*nails_radius], true);
    
    //all TX nails bottom left
    translate([-10*i, -370, 0])
        square([2*nails_radius, 2*nails_radius], true);
}

x_silicon_offset = 10;
x_max = 250;

polygon([ [x_max, 0], for(i = [0:35.9]) [RX_x[i] + x_silicon_offset, -10*i], [x_max, -350]]);
    
polygon([ [-x_max, 0], for(i = [0:35.9]) [-RX_x[i] - x_silicon_offset, -10*i], [-x_max, -350]]);

polygon([[x_max, -350], [RX_x[35] + x_silicon_offset, -350], [RX_x[35], -375],[x_max, -375]]);

polygon([[-x_max, -350], [-RX_x[35]  -x_silicon_offset, -350], [-RX_x[35], -375],[-x_max, -375]]);

polygon([[x_max, 0], [RX_x[0] + x_silicon_offset, 0], [RX_x[0] +x_silicon_offset, 25],[x_max, 25]]);

polygon([[-x_max, 0], [-RX_x[0] - x_silicon_offset, 0], [-RX_x[0] -x_silicon_offset, 25],[-x_max, 25]]);



