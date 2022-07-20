$fn = 20;
min_radius = 0.835;
max_radius = 0.835;
radius = 0.9;
nozzle = 0.47;
nb = 7;

for(i = [0 : nb-1], j = [0 : nb-1]){

translate([3*(radius+nozzle)*i,3*(radius+nozzle)*j,0]) difference() {
    linear_extrude(0.6) circle(r = radius+nozzle);
    linear_extrude(0.8) circle(r = radius);

}

}