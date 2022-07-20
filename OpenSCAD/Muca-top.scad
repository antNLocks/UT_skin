muca_w = 24;
muca_h = 44.8;
muca_t = 2;
nozzle = 0.46;

cube([44.8+2*nozzle, 10, 0.2]);
linear_extrude(muca_w){
square([2*nozzle + muca_h, nozzle]);
square([nozzle, 2*nozzle + muca_t]);
translate([0, nozzle + muca_t])
    square([1.5, nozzle]);

translate([nozzle + muca_h, 0]){
    square([nozzle, 2*nozzle + muca_t]);
translate([-1.5+nozzle, nozzle + muca_t])
    square([1.5, nozzle]);
}
}